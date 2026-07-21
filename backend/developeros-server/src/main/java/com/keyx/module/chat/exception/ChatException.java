package com.keyx.module.chat.exception;

import com.keyx.common.exception.BusinessException;
import lombok.Getter;

/**
 * Chat 模块业务异常
 *
 * 继承 BusinessException，错误码用 1500+ 段
 * 方便识别是 Chat 模块的异常
 *
 * 错误码规划：
 *   1500 = 通用 Chat 错误
 *   1501 = 会话不存在
 *   1502 = 消息不存在
 *   1503 = 消息状态错（不在生成中）
 *   1504 = AI 调用失败
 *   1505 = 流式响应失败
 *
 * 使用示例：
 * <pre>
 *   throw new ChatException("AI 调用失败");
 *   throw new ChatException(1501, "会话不存在");
 * </pre>
 */
@Getter
public class ChatException extends BusinessException {

    public ChatException(String message) {
        super(message);
    }

    public ChatException(Integer code, String message) {
        super(code, message);
    }

    // ============================================
    // 常用错误码静态方法（让调用方更清晰）
    // ============================================

    /**
     * 会话不存在
     */
    public static ChatException conversationNotFound() {
        return new ChatException(1501, "会话不存在");
    }

    /**
     * 消息不存在
     */
    public static ChatException messageNotFound() {
        return new ChatException(1502, "消息不存在");
    }

    /**
     * 消息状态错（不在生成中）
     */
    public static ChatException messageNotStreaming() {
        return new ChatException(1503, "消息不在生成中");
    }

    /**
     * AI 调用失败
     */
    public static ChatException aiCallFailed(String detail) {
        return new ChatException(1504, "AI 调用失败：" + detail);
    }

    /**
     * 流式响应失败
     */
    public static ChatException streamFailed(String detail) {
        return new ChatException(1505, "流式响应失败：" + detail);
    }
}