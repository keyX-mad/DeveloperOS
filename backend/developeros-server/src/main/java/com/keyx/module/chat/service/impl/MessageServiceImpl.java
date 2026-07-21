package com.keyx.module.chat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keyx.common.exception.BusinessException;
import com.keyx.module.chat.entity.Conversation;
import com.keyx.module.chat.entity.Message;
import com.keyx.module.chat.enums.MessageRole;
import com.keyx.module.chat.enums.MessageStatus;
import com.keyx.module.chat.mapper.MessageMapper;
import com.keyx.module.chat.service.ConversationService;
import com.keyx.module.chat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 消息 Service 实现
 *
 * 核心安全要求：所有方法都必须先校验 userId 归属！
 * - 防止 A 用户访问/修改/删除 B 用户的消息
 * - 校验失败统一抛 404（不是 403），防止信息泄露
 *
 * 状态机（assistant 消息）：
 *   saveAssistantStart  → status = STREAMING（中间态）
 *   ↓
 *   completeAssistant   → status = COMPLETED（终态）
 *   failAssistant       → status = FAILED（终态）
 *   stopAssistant       → status = STOPPED（终态）
 *
 * #8 修复：不再直接注入 ConversationMapper，
 *        改用 ConversationService.touchLastMessage 复用 Service 层校验。
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    MessageMapper messageMapper;

    @Autowired
    ConversationService conversationService;

    // ✘ 删除了 ConversationMapper 的直接注入（#8 修复点）

    // ============================================
    // ① 原子入口：流式对话用（#4 修复点）
    // ============================================

    /**
     * 原子地保存用户消息 + 创建 AI 占位消息
     *
     * 两个 insert + 两个 update 在同一个事务里：
     * - 任何一步失败都会全部回滚
     * - 不会出现"用户消息已存但 AI 占位没存"的不一致
     *
     * @return [userMessage, assistantMessage]
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Message> saveUserAndAssistantStart(Long userId, Long conversationId, String content) {
        Message userMsg = createUserMessageInternal(userId, conversationId, content);
        Message assistantMsg = createAssistantStartInternal(userId, conversationId);
        return List.of(userMsg, assistantMsg);
    }

    // ============================================
    // ② 公开方法（保持向后兼容，内部委托给私有方法）
    // ============================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message saveUserMessage(Long userId, Long conversationId, String content) {
        return createUserMessageInternal(userId, conversationId, content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message saveAssistantStart(Long userId, Long conversationId) {
        return createAssistantStartInternal(userId, conversationId);
    }

    // ============================================
    // ③ 内部方法（不带 @Transactional，由调用方决定事务边界）
    // ============================================

    /**
     * 创建用户消息 + 触发会话 lastMessageAt 更新
     * 内部方法，不加 @Transactional，由调用方控制事务
     */
    private Message createUserMessageInternal(Long userId, Long conversationId, String content) {
        // 1. 权限校验（复用 ConversationService.getById）
        conversationService.getById(userId, conversationId);

        // 2. 创建用户消息
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(MessageRole.USER);
        message.setStatus(MessageStatus.COMPLETED);
        message.setContent(content);
        // 手动设置时间字段（V1 暂不依赖 MyMetaObjectHandler）
        Instant now = Instant.now();
        message.setCreatedAt(now);
        message.setUpdatedAt(now);

        // 3. 插入消息
        messageMapper.insert(message);

        // 4. 更新会话的"最后消息时间"（通过 Service 层，#8 修复点）
        conversationService.touchLastMessage(userId, conversationId);

        return message;
    }

    /**
     * 创建 STREAMING 状态的 ASSISTANT 消息（占位）+ 触发 lastMessageAt 更新
     */
    private Message createAssistantStartInternal(Long userId, Long conversationId) {
        // 1. 权限校验
        conversationService.getById(userId, conversationId);

        // 2. 创建 STREAMING 消息（占位，AI 还没开始生成）
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(MessageRole.ASSISTANT);
        message.setStatus(MessageStatus.STREAMING);
        message.setContent("");
        // 手动设置时间字段
        Instant now = Instant.now();
        message.setCreatedAt(now);
        message.setUpdatedAt(now);

        // 3. 插入消息
        messageMapper.insert(message);

        // 4. 更新会话的"最后消息时间"（通过 Service 层，#8 修复点）
        conversationService.touchLastMessage(userId, conversationId);

        return message;
    }

    // ============================================
    // ④ 状态机流转：STREAMING → 终态
    // ============================================

    /**
     * AI 生成完成：STREAMING → COMPLETED
     * content 存完整文本，tokenCount 可空
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message completeAssistant(Long userId, Long messageId, String content, Integer tokenCount) {
        // 1. 查消息 + null 检查
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }

        // 2. 权限校验
        conversationService.getById(message.getConversationId(), userId);

        // 3. 状态校验：必须是 STREAMING
        if (!message.getStatus().equals(MessageStatus.STREAMING)) {
            throw new BusinessException(400, "消息不在生成中");
        }

        // 4. 更新字段
        message.setStatus(MessageStatus.COMPLETED);
        message.setContent(content);
        message.setTokenCount(tokenCount);

        // 5. 保存到数据库
        messageMapper.updateById(message);

        return message;
    }

    /**
     * AI 生成失败：STREAMING → FAILED
     * 关键：content 字段不修改（保留 AI 已生成的部分）
     * 错误原因存到 errorMessage 字段
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message failAssistant(Long userId, Long messageId, String errorMessage) {
        // 1. 查消息 + null 检查
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }

        // 2. 权限校验
        conversationService.getById(message.getConversationId(), userId);

        // 3. 状态校验：必须是 STREAMING
        if (!message.getStatus().equals(MessageStatus.STREAMING)) {
            throw new BusinessException(400, "消息不在生成中");
        }

        // 4. 更新字段
        message.setStatus(MessageStatus.FAILED);
        message.setErrorMessage(errorMessage);

        // 5. 保存到数据库
        messageMapper.updateById(message);

        return message;
    }

    /**
     * 用户主动停止：STREAMING → STOPPED
     * 关键：把 AI 已生成的部分内容存到 content 字段
     * （用户能看到"AI 写到一半被停了"）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message stopAssistant(Long userId, Long messageId, String partialContent) {
        // 1. 查消息 + null 检查
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }

        // 2. 权限校验
        conversationService.getById(message.getConversationId(), userId);

        // 3. 状态校验：必须是 STREAMING
        if (!message.getStatus().equals(MessageStatus.STREAMING)) {
            throw new BusinessException(400, "消息不在生成中");
        }

        // 4. 更新字段
        message.setStatus(MessageStatus.STOPPED);
        message.setContent(partialContent);

        // 5. 保存到数据库
        messageMapper.updateById(message);

        return message;
    }

    // ============================================
    // ⑤ 查询
    // ============================================

    /**
     * 查询某个会话的所有消息（分页）
     * 安全：必校验 userId 归属
     * 排序：按时间正序（聊天从早到晚）
     *
     * ⚠️ Workaround：MyBatis-Plus 3.5.5 + MyBatis 3.5.16 OGNL sandbox 不兼容
     * - records 用 @Select 手写 SQL（selectList 会触发 OGNL）
     * - total 用链式 lambdaQuery().count()（链式走不同代码路径，绕开 OGNL）
     */
    @Override
    public Page<Message> listByConversation(Long userId, Long conversationId, int current, int size) {
        // 1. 权限校验
        conversationService.getById(userId, conversationId);

        // 2. 查当前页（@Select 手写，selectList 触发 OGNL 走不通）
        List<Message> records = messageMapper.selectByConversationPaged(conversationId, size, (long) (current - 1) * size);

        // 3. 查总数（链式 .count() 能用！比 @Select 简洁）
        long total = lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .eq(Message::getStatus, MessageStatus.COMPLETED)
                .count();

        // 4. 组装 Page
        Page<Message> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    // ============================================
    // ⑥ 辅助方法（#6 abort 修复用）
    // ============================================

    /**
     * 根据 messageId 查询，并校验当前用户是否有权访问
     * （abort 接口需要先校验归属，防止 A 用户中止 B 用户的生成）
     *
     * @throws BusinessException 404 当消息不存在或不属于当前用户
     */
    @Override
    public Message getByIdForUser(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }
        // 复用 ConversationService.getById 做归属校验
        conversationService.getById(userId, message.getConversationId());
        return message;
    }
}
