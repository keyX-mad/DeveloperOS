package com.keyx.module.chat.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keyx.common.exception.BusinessException;
import com.keyx.module.chat.dto.request.CreateConversationRequest;
import com.keyx.module.chat.dto.request.UpdateConversationRequest;
import com.keyx.module.chat.entity.Conversation;
import com.keyx.module.chat.mapper.ConversationMapper;
import com.keyx.module.chat.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 会话 Service 实现
 *
 * 核心安全要求：所有方法都必须先校验 userId 归属！
 * - 防止 A 用户访问/修改/删除 B 用户的会话
 * - 校验失败统一抛 404（不是 403），防止信息泄露
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService  {

    @Autowired
    private ConversationMapper conversationMapper;




    @Override
    public Conversation create(Long userId, CreateConversationRequest req) {
        String title = req.getTitle();
        if (title ==null || title.isBlank()){
            title = "New Conversation";
        }

        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setStatus("active");

        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public Page<Conversation> list(Long userId, int current, int size) {
        Page<Conversation> page = new Page<>(current, size);
        return conversationMapper.selectPage(page,
                lambdaQuery()
                        .eq(Conversation::getUserId, userId)
                        .orderByDesc(Conversation::getUpdatedAt)
        );
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
     *
     * 安全校验（核心！）：
     * 1. 校验会话存在
     * 2. 校验会话归属当前用户
     * 失败时抛 404（不是 403）
     */
    @Override
    public Conversation update(Long userId, Long conversationId, UpdateConversationRequest req) {
        // 1. 查实体
        Conversation conversation = conversationMapper.selectById(conversationId);

        // 2. null 检查 + userId 归属校验
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(404, "会话不存在");
        }

        // 3. 只改 title（不改 status 等其他字段）
        conversation.setTitle(req.getTitle());

        // 4. 持久化
        conversationMapper.updateById(conversation);

        return conversation;
    }

    /**
     * 删除会话
     *
     * 安全校验（核心！）：
     * 1. 校验会话存在
     * 2. 校验会话归属当前用户
     * 失败时抛 404
     *
     * 事务：删 conversation 会级联删 messages
     * 加 @Transactional 保证两个操作一起成功或一起失败
     */
    @Override
    @Transactional
    public void delete(Long userId, Long conversationId) {
        // 1. 查实体
        Conversation conversation = conversationMapper.selectById(conversationId);

        // 2. null 检查 + userId 归属校验
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(404, "会话不存在");
        }

        // 3. 删除（数据库级联删除 messages）
        conversationMapper.deleteById(conversationId);
    }
}