package com.keyx.module.chat.dto.response;

import com.keyx.module.chat.enums.MessageRole;
import com.keyx.module.chat.enums.MessageStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息响应 VO
 *
 * 后端返回给前端的 JSON 长这样：
 * {
 *   "id": 1,
 *   "role": "user",                // 枚举自动转小写字符串
 *   "content": "你好",
 *   "status": "completed",         // 枚举自动转小写字符串
 *   "modelName": "gpt-4o-mini",
 *   "tokenCount": 10,
 *   "createdAt": "2026-07-18 14:30:00"
 * }
 *
 * role 和 status 用枚举（不是 String），通过 @JsonValue 输出小写字符串
 */
@Data
public class MessageVO {

    /**
     * 消息 ID
     */
    private Long id;

    /**
     * 消息角色
     * 用枚举 MessageRole，JSON 序列化为 "system" / "user" / "assistant" / "tool"
     */
    private MessageRole role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息状态
     * 用枚举 MessageStatus，JSON 序列化为 "streaming" / "completed" / "failed" / "stopped"
     */
    private MessageStatus status;

    /**
     * 生成该消息的 AI 模型
     * 用户消息为 null
     */
    private String modelName;

    /**
     * Token 数量（用于费用统计 / 上下文控制）
     */
    private Integer tokenCount;

    /**
     * 消息创建时间
     * 用 LocalDateTime（前端友好）
     */
    private LocalDateTime createdAt;
}
