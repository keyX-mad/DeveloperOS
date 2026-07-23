package com.keyx.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keyx.common.exception.BusinessException;
import com.keyx.module.chat.dto.request.CreateConversationRequest;
import com.keyx.module.chat.dto.request.UpdateConversationRequest;
import com.keyx.module.chat.entity.Conversation;
import com.keyx.module.chat.enums.ConversationStatus;
import com.keyx.module.chat.mapper.ConversationMapper;
import com.keyx.module.chat.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 会话 Service 实现
 *
 * 核心安全要求：所有方法都必须先校验 userId 归属！
 * - 防止 A 用户访问/修改/删除 B 用户的会话
 * - 校验失败统一抛 404（不是 403），防止信息泄露
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    /** 当前实体对应的表名（V1 临时方案：业务代码手动传） */
    private static final String TABLE_NAME = "conversation";

    @Autowired
    private ConversationMapper conversationMapper;

    @Override
    public Conversation create(Long userId, CreateConversationRequest req) {
        String title = req.getTitle();
        if (title == null || title.isBlank()) {
            title = "New Conversation";
        }

        Instant now = Instant.now();
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setStatus(ConversationStatus.ACTIVE);   // ← 改用枚举
        // 手动设置时间字段（V1 暂不依赖 MyMetaObjectHandler）
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);

        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public Page<Conversation> list(Long userId, int current, int size) {
        Page<Conversation> page = new Page<>(current, size);
        LambdaQueryWrapper<Conversation> wrapper = Wrappers.lambdaQuery(Conversation.class)
                .eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getUpdatedAt);

        return conversationMapper.selectPage(page, wrapper);
    }

    /**
     * 根据 ID 查询会话
     *
     * 安全校验（核心！）：
     * 1. 校验会话存在
     * 2. 校验会话归属当前用户
     * 失败时抛 404（不是 403），防止信息泄露
     */
    @Override
    public Conversation getById(Long userId, Long conversationId) {
        // 1. 查实体
        Conversation conversation = conversationMapper.selectById(conversationId);

        // 2. null 检查 + userId 归属校验（任一失败都抛 404）
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(404, "会话不存在");
        }

        // 3. 返回实体
        return conversation;
    }

    /**
     * 更新会话标题
     */
    @Override
    public Conversation update(Long userId, Long conversationId, UpdateConversationRequest req) {
        // 1. 查实体（getById 内部已做 null + userId 校验）
        Conversation conversation = getById(userId, conversationId);

        // 2. 只改 title（不改 status 等其他字段）
        conversation.setTitle(req.getTitle());

        // 3. 持久化
        conversationMapper.updateById(conversation);

        return conversation;
    }

    /**
     * 删除会话
     * 删 conversation 会级联删 messages（数据库外键 ON DELETE CASCADE）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long conversationId) {
        // 1. 校验归属（getById 内部已做）
        getById(userId, conversationId);

        // 2. 删除（数据库级联删除 messages）
        conversationMapper.deleteById(conversationId);
    }

    // ============================================
    // #8 修复：touchLastMessage（MessageService 专用入口）
    // ============================================

    /**
     * 更新会话的"最后消息时间"（派生字段）
     * 仅供 MessageService 调用，避免 MessageService 直接注入 ConversationMapper 绕过 Service 层
     *
     * @param userId          当前用户 ID（用于校验归属）
     * @param conversationId  会话 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void touchLastMessage(Long userId, Long conversationId) {
        // 1. 校验归属（getById 内部已做）
        Conversation conversation = getById(userId, conversationId);

        // 2. 更新 lastMessageAt（触发器自动维护 updated_at）
        conversation.setLastMessageAt(Instant.now());
        conversationMapper.updateById(conversation);
    }
}
