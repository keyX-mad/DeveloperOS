package com.keyx.module.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.module.chat.entity.Message;

import java.util.List;

/**
 * 消息 Service 接口
 *
 * 核心约定：
 * - 所有方法第一个参数都是 userId（当前登录用户 ID）
 * - 实现类内部必须做 userId 归属校验
 * - 校验失败统一抛 BusinessException 404
 *
 * 状态机（assistant 消息）：
 *   saveAssistantStart       → STREAMING（中间态）
 *     ↓
 *   completeAssistant        → COMPLETED（终态）
 *   failAssistant            → FAILED（终态）
 *   stopAssistant            → STOPPED（终态）
 */
public interface MessageService {

    /**
     * 保存用户消息
     * 同时更新会话的 lastMessageAt
     *
     * @param userId         当前登录用户 ID
     * @param conversationId 会话 ID
     * @param content        消息内容
     * @return 创建好的用户消息
     */
    Message saveUserMessage(Long userId, Long conversationId, String content);

    /**
     * 创建 AI 占位消息（STREAMING 状态）
     * 用于流式生成开始时记录一条空消息，后续用 completeAssistant / failAssistant / stopAssistant 更新
     *
     * @param userId         当前登录用户 ID
     * @param conversationId 会话 ID
     * @return 创建好的 AI 占位消息（含 DB 自动生成的 id）
     */
    Message saveAssistantStart(Long userId, Long conversationId);

    /**
     * AI 生成完成：STREAMING → COMPLETED
     * 把累积的完整文本写入 content
     *
     * @param userId      当前登录用户 ID
     * @param messageId   消息 ID
     * @param content     完整生成内容
     * @param tokenCount  Token 数量（可空）
     * @return 更新后的消息
     */
    Message completeAssistant(Long userId, Long messageId, String content, Integer tokenCount);

    /**
     * AI 生成失败：STREAMING → FAILED
     * 保留已生成的部分 content，把错误原因存到 errorMessage
     *
     * @param userId        当前登录用户 ID
     * @param messageId     消息 ID
     * @param errorMessage  失败原因摘要
     * @return 更新后的消息
     */
    Message failAssistant(Long userId, Long messageId, String errorMessage);

    /**
     * 用户主动停止：STREAMING → STOPPED
     * 把已生成的部分内容存到 content（用户能看到"AI 写到一半被停了"）
     *
     * @param userId         当前登录用户 ID
     * @param messageId      消息 ID
     * @param partialContent 已生成的部分内容
     * @return 更新后的消息
     */
    Message stopAssistant(Long userId, Long messageId, String partialContent);

    /**
     * 查询某个会话的所有消息（分页）
     * 实现类内部必须校验会话归属当前用户
     *
     * @param userId         当前登录用户 ID
     * @param conversationId 会话 ID
     * @param current        当前页码（从 1 开始）
     * @param size           每页大小
     * @return 分页结果（按时间正序）
     */
    Page<Message> listByConversation(Long userId, Long conversationId, int current, int size);

    /**
     * 原子地保存用户消息 + 创建 AI 占位消息
     * 用于流式对话入口，保证两个 insert 要么都成功要么都失败
     *
     * @param userId         当前登录用户 ID
     * @param conversationId 会话 ID
     * @param content        用户消息内容
     * @return [userMessage, assistantMessage] 两条消息
     */
    List<Message> saveUserAndAssistantStart(Long userId, Long conversationId, String content);

    /**
     * 根据 messageId 查询消息，并校验当前用户归属
     * 用于 abort 等需要先确认消息归属的接口
     *
     * @param userId    当前登录用户 ID
     * @param messageId 消息 ID
     * @return 消息实体
     * @throws com.keyx.common.exception.BusinessException 404 当消息不存在或不属于当前用户
     */
    Message getByIdForUser(Long userId, Long messageId);
}
