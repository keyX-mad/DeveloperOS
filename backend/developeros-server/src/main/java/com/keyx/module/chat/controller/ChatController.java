package com.keyx.module.chat.controller;

import com.keyx.common.BaseController;
import com.keyx.common.R;
import com.keyx.module.chat.dto.request.SendMessageRequest;
import com.keyx.module.chat.service.MessageService;
import com.keyx.module.chat.stream.ChatStreamChunk;
import com.keyx.module.chat.stream.ChatStreamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Chat Controller（流式接口 + abort）
 *
 * 暴露 2 个接口：
 * - POST /api/chat/conversations/{conversationId}/messages   SSE 流式对话
 * - POST /api/chat/abort/{messageId}                          用户停止生成
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController extends BaseController {

    @Autowired
    private ChatStreamService chatStreamService;

    @Autowired
    private MessageService messageService;

    /**
     * ① SSE 流式对话
     *
     * 前端用 EventSource 或 fetch + ReadableStream 接收
     * 每个 chunk 格式：
     *   {"type":"content","content":"一小块文本"}
     *   {"type":"done","done":true}    ← 流结束
     *   {"type":"error","error":"..."}  ← 出错
     */
    @PostMapping(value = "/conversations/{conversationId}/messages",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatStreamChunk> stream(@PathVariable Long conversationId,
                                        @Valid @RequestBody SendMessageRequest req) {
        Long userId = currentUserId();
        return chatStreamService.stream(userId, conversationId, req.getContent(), req.getModelName());
    }

    /**
     * ② 用户主动停止生成（#6 完整修复）
     *
     * 1. 先校验 message 归属当前用户（防止 A 用户中止 B 用户的生成）
     * 2. 调 chatStreamService.markAbort 设置 abort 标记
     * 3. 标记后流式回调 doOnNext 检查到标记会抛 AbortSignal 中断流
     * 4. doOnError 捕获 AbortSignal 调 stopAssistant 写库
     */
    @PostMapping("/abort/{messageId}")
    public R<Void> abort(@PathVariable Long messageId) {
        Long userId = currentUserId();
        // 1. 校验归属（抛 404 防止越权）
        messageService.getByIdForUser(userId, messageId);
        // 2. 标记为停止（由 ChatStreamServiceImpl 实际检查并中断流）
        chatStreamService.markAbort(messageId);
        return R.ok();
    }
}
