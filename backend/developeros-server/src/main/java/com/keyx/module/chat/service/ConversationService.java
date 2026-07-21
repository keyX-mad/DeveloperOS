package com.keyx.module.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.module.chat.dto.request.CreateConversationRequest;
import com.keyx.module.chat.dto.request.UpdateConversationRequest;
import com.keyx.module.chat.entity.Conversation;

/**
 * 会话 Service 接口
 *
 * 核心约定：
 * - 所有方法第一个参数都是 userId（当前登录用户 ID）
 * - 实现类内部必须做 userId 归属校验，防止越权
 * - 校验失败统一抛 BusinessException 404（不抛 403，防止信息泄露）
 */
public interface ConversationService {

    /**
     * 创建新会话
     *
     * @param userId 当前登录用户 ID
     * @param req    创建请求（含 title，可选）
     * @return 创建好的会话（已包含 DB 自动生成的 id、createdAt 等）
     */
    Conversation create(Long userId, CreateConversationRequest req);

    /**
     * 分页查询当前用户的所有会话
     *
     * @param userId  当前登录用户 ID
     * @param current 当前页码（从 1 开始）
     * @param size    每页大小
     * @return 分页结果（含 records / total / current / size）
     */
    Page<Conversation> list(Long userId, int current, int size);

    /**
     * 根据 ID 查询会话
     * 实现类内部必须校验会话归属当前用户
     *
     * @param userId         当前登录用户 ID
     * @param conversationId 会话 ID
     * @return 会话实体
     * @throws com.keyx.common.exception.BusinessException 404 当会话不存在或不属于当前用户
     */
    Conversation getById(Long userId, Long conversationId);

    /**
     * 更新会话标题
     * 实现类内部必须校验会话归属当前用户
     *
     * @param userId         当前登录用户 ID
     * @param conversationId 会话 ID
     * @param req            更新请求（含新 title）
     * @return 更新后的会话
     */
    Conversation update(Long userId, Long conversationId, UpdateConversationRequest req);

    /**
     * 删除会话（数据库级联删除 messages）
     * 实现类内部必须校验会话归属当前用户
     *
     * @param userId         当前登录用户 ID
     * @param conversationId 会话 ID
     */
    void delete(Long userId, Long conversationId);

    /**
     * 更新会话的"最后消息时间"
     * 仅供 MessageService 调用（避免直绕 mapper 违反分层）
     *
     * @param userId         当前登录用户 ID
     * @param conversationId 会话 ID
     */
    void touchLastMessage(Long userId, Long conversationId);
}
