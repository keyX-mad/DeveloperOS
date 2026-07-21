package com.keyx.module.chat.stream.impl;

import com.keyx.module.chat.entity.Message;
import com.keyx.module.chat.prompt.PromptBuilder;
import com.keyx.module.chat.service.ConversationService;
import com.keyx.module.chat.service.MessageService;
import com.keyx.module.chat.stream.ChatStreamChunk;
import com.keyx.module.chat.stream.ChatStreamService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat 流式服务实现
 *
 * 核心职责：编排"用户发消息 → AI 流式回复"完整流程
 *
 * 流程：
 *   1. 校验 conversation 归属
 *   2. 原子保存 user 消息 + AI 占位（同一事务）
 *   3. 构建 Prompt
 *   4. 调 LLM 流式生成
 *   5. 累积 token → 推给前端
 *   6. 完成时一次性写库 / 失败时记 error
 *
 * 异常路径：
 *   - LLM 异常 → failAssistant（status=FAILED）
 *   - 用户主动 abort → stopAssistant（status=STOPPED，#6 修复点）
 *   - 程序崩溃 → 消息留在 STREAMING（V1 暂不处理，TODO V2 定时清理）
 */
@Service
public class ChatStreamServiceImpl implements ChatStreamService {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final PromptBuilder promptBuilder;
    private final ChatClient chatClient;   // ← 单例（#9 修复点：构造时 build 一次）

    /**
     * abort 标记（in-memory 存储）
     * key = assistantMessageId，value = true 表示已停止
     * V1 单实例用 ConcurrentHashMap；V2 改 Redis 解决多实例问题
     */
    private final ConcurrentHashMap<Long, Boolean> abortFlags = new ConcurrentHashMap<>();

    public ChatStreamServiceImpl(ConversationService conversationService,
                                  MessageService messageService,
                                  PromptBuilder promptBuilder,
                                  ChatClient.Builder chatClientBuilder) {
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.promptBuilder = promptBuilder;
        this.chatClient = chatClientBuilder.build();   // ← 构造时只 build 一次
    }

    @Override
    public Flux<ChatStreamChunk> stream(Long userId, Long conversationId,
                                        String userContent, String modelName) {
        // 1. 校验 conversation 归属
        conversationService.getById(userId, conversationId);

        // 2. 原子保存 user 消息 + AI 占位（#4 修复点：同一事务）
        List<Message> msgs = messageService.saveUserAndAssistantStart(userId, conversationId, userContent);
        Message assistantMessage = msgs.get(1);   // [user, assistant]
        Long assistantMessageId = assistantMessage.getId();

        // 3. 拿 Prompt（TODO V2: modelName 用于选择 ChatClient）
        Prompt prompt = promptBuilder.build(conversationId, userContent);

        // 4. 累积器
        StringBuilder fullContent = new StringBuilder();

        // 5. 调 AI 流式生成（#6 修复点：检查 abortFlags）
        return chatClient
                .prompt(prompt)
                .stream()
                .content()                                    // Flux<String>
                .doOnNext(text -> {
                    // 检查 abort 信号
                    if (Boolean.TRUE.equals(abortFlags.get(assistantMessageId))) {
                        throw new AbortSignal();   // 抛异常中断流
                    }
                    fullContent.append(text);
                })
                .map(text -> ChatStreamChunk.content(text))    // 包装成 chunk 推给前端
                .doOnComplete(() -> {
                    // 流式正常完成
                    String finalContent = fullContent.toString();
                    messageService.completeAssistant(userId, assistantMessageId, finalContent, null);
                    abortFlags.remove(assistantMessageId);      // 清理 abort 标记
                })
                .doOnError(error -> {
                    if (error instanceof AbortSignal) {
                        // 用户主动停止：把已生成的部分存为 STOPPED
                        messageService.stopAssistant(userId, assistantMessageId, fullContent.toString());
                    } else {
                        // 真正的异常：标记 FAILED
                        messageService.failAssistant(userId, assistantMessageId, error.getMessage());
                    }
                    abortFlags.remove(assistantMessageId);
                });
    }

    /**
     * 标记消息为"已停止"（由 ChatController.abort 调用）
     * 下一次 doOnNext 检查到后抛 AbortSignal 中断流
     */
    @Override
    public void markAbort(Long assistantMessageId) {
        abortFlags.put(assistantMessageId, true);
    }

    /**
     * 内部异常类：用于在流式过程中抛出来中断流
     * 不会作为"真正的错误"展示给用户
     */
    private static class AbortSignal extends RuntimeException {
        public AbortSignal() {
            super("user abort");
        }
    }
}
