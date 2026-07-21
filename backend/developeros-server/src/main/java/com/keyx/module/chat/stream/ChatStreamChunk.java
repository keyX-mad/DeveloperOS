package com.keyx.module.chat.stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 流式响应的每个 chunk
 * 推给前端的数据结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatStreamChunk {

    /**
     * 类型：content / done / error
     */
    private String type;

    /**
     * 内容（一小块文本）
     */
    private String content;

    /**
     * 是否完成（type=done 时为 true）
     */
    private Boolean done;

    /**
     * 错误信息（type=error 时有值）
     */
    private String error;

    // ============================================
    // 工厂方法（让创建更简洁）
    // ============================================

    /**
     * 创建 content chunk（AI 正在生成的一小块）
     */
    public static ChatStreamChunk content(String content) {
        return new ChatStreamChunk("content", content, false, null);
    }

    /**
     * 创建 done chunk（流结束标记）
     */
    public static ChatStreamChunk done() {
        return new ChatStreamChunk("done", null, true, null);
    }

    /**
     * 创建 error chunk（出错标记）
     */
    public static ChatStreamChunk error(String error) {
        return new ChatStreamChunk("error", null, false, error);
    }
}
