package com.keyx.module.chat.stream;

import reactor.core.publisher.Flux;

/**
 * Chat 流式服务接口
 *
 * 负责流式对话的核心编排：
 * - stream: 用户发送消息，返回 Flux<ChatStreamChunk>，边生成边推送
 * - markAbort: 标记某条消息为"已停止"，由 ChatController.abort 调用
 *
 * 实现类内部用 Spring AI 的 ChatClient 做底层 LLM 调用
 */
public interface ChatStreamService {

    /**
     * 流式对话：用户发送一条消息，返回 Flux<ChatStreamChunk>
     *
     * @param userId         当前登录用户 ID
     * @param conversationId 会话 ID
     * @param userContent    用户消息内容
     * @param modelName      指定模型（可空，留给 V2 多模型选择用）
     * @return 流式 chunk 序列，每个 chunk 含 type/content/done/error
     */
    Flux<ChatStreamChunk> stream(Long userId, Long conversationId,
                                 String userContent, String modelName);

    /**
     * 标记某条 assistant 消息为"已停止"
     * 下一次 ChatClient 流式回调检查到标记后会抛 AbortSignal 中断流
     *
     * @param assistantMessageId AI 消息 ID
     */
    void markAbort(Long assistantMessageId);
}
