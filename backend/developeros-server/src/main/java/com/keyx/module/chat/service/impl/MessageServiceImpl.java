package com.keyx.module.chat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keyx.common.exception.BusinessException;
import com.keyx.module.chat.entity.Conversation;
import com.keyx.module.chat.entity.Message;
import com.keyx.module.chat.enums.MessageRole;
import com.keyx.module.chat.enums.MessageStatus;
import com.keyx.module.chat.mapper.ConversationMapper;
import com.keyx.module.chat.mapper.MessageMapper;
import com.keyx.module.chat.service.ConversationService;
import com.keyx.module.chat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>  implements MessageService {

    @Autowired
    MessageMapper messageMapper;
    @Autowired
    ConversationService conversationService;
    @Autowired
    ConversationMapper conversationMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message saveUserMessage(Long userId, Long conversationId, String content) {
        // 1. 权限校验（复用 ConversationService.getById，内部已做 null + userId 校验）
        Conversation conversation = conversationService.getById(userId, conversationId);

        // 2. 创建用户消息
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(MessageRole.USER);
        message.setStatus(MessageStatus.COMPLETED);
        message.setContent(content);

        // 3. 插入消息
        messageMapper.insert(message);

        // 4. 更新会话的"最后消息时间"（派生字段，Service 维护）
        //    必须和 insert 在同一事务！
        conversation.setLastMessageAt(Instant.now());
        conversationMapper.updateById(conversation);

        return message;
    }

    /**
     * AI 开始生成：创建 STREAMING 状态的 ASSISTANT 消息（占位）
     *
     * 状态机：
     *   saveAssistantStart → status = STREAMING
     *   ↓
     *   completeAssistant / failAssistant / stopAssistant
     *   → status = COMPLETED / FAILED / STOPPED
     *
     * 事务：插消息 + 更新 lastMessageAt 必须同一事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message saveAssistantStart(Long userId, Long conversationId) {
        // 1. 权限校验（复用 ConversationService.getById）
        Conversation conversation = conversationService.getById(userId, conversationId);

        // 2. 创建 STREAMING 消息（占位，AI 还没开始生成）
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(MessageRole.ASSISTANT);      // AI 角色
        message.setStatus(MessageStatus.STREAMING);   // 生成中（中间态）
        message.setContent("");                        // 空字符串，不是 null

        // 3. 插入消息（id 自动生成）
        messageMapper.insert(message);

        // 4. 更新会话的"最后消息时间"（与 insert 同一事务）
        conversation.setLastMessageAt(Instant.now());
        conversationMapper.updateById(conversation);

        // 5. 返回消息（前端拿 messageId 后续调用 complete/fail/stop）
        return message;
    }

    /**
     * AI 生成完成：把 STREAMING 消息改成 COMPLETED
     *
     * 状态机：STREAMING → COMPLETED
     *
     * 不加 @Transactional：流式完成后失败时保留已存内容
     * （用户能看到"生成失败但写了一部分"）
     */
    @Override
    public Message completeAssistant(Long userId, Long messageId, String content, Integer tokenCount) {
        // 1. 查消息 + null 检查
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }

        // 2. 权限校验（复用 ConversationService.getById，内部做 null + userId 校验）
        conversationService.getById(message.getConversationId(), userId);

        // 3. 状态校验：必须是 STREAMING（中间态 → 终态）
        if (!message.getStatus().equals(MessageStatus.STREAMING)) {
            throw new BusinessException(400, "消息不在生成中");
        }

        // 4. 更新字段
        message.setStatus(MessageStatus.COMPLETED);
        message.setContent(content);
        message.setTokenCount(tokenCount);

        // 5. 保存到数据库
        messageMapper.updateById(message);

        // 6. 不更新 lastMessageAt（saveAssistantStart 时已经更新过）

        return message;
    }

    /**
     * AI 生成失败：把 STREAMING 消息改成 FAILED
     *
     * 状态机：STREAMING → FAILED
     *
     * 关键：content 字段不修改（保留 AI 已生成的部分）
     * 错误原因存到 errorMessage 字段
     */
    @Override
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
        message.setErrorMessage(errorMessage);   // ← 存到 errorMessage 字段（不是 content！）

        // 5. 保存到数据库
        messageMapper.updateById(message);

        return message;
    }

    /**
     * 用户主动停止：把 STREAMING 消息改成 STOPPED
     *
     * 状态机：STREAMING → STOPPED
     *
     * 关键：把 AI 已生成的部分内容存到 content 字段
     * （用户能看到"AI 写到一半被停了"）
     */
    @Override
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
        message.setContent(partialContent);   // ← 存到 content 字段（已生成的部分）

        // 5. 保存到数据库
        messageMapper.updateById(message);

        return message;
    }

    /**
     * 查询某个会话的所有消息（分页）
     *
     * 安全：必校验 userId 归属
     * 排序：按时间正序（聊天从早到晚）
     */
    @Override
    public Page<Message> listByConversation(Long userId, Long conversationId, int current, int size) {
        // 1. 权限校验（复用 ConversationService.getById）
        conversationService.getById(userId, conversationId);

        // 2. 分页查询（按时间正序）
        Page<Message> page = new Page<>(current, size);
        return messageMapper.selectPage(page,
                lambdaQuery()
                        .eq(Message::getConversationId, conversationId)
                        .orderByAsc(Message::getCreatedAt)
        );
    }
}
