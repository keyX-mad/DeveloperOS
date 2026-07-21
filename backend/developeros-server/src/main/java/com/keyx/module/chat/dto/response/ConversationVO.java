package com.keyx.module.chat.dto.response;

import com.keyx.module.chat.enums.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话响应 VO
 *
 * 后端返回给前端的 JSON 长这样：
 * {
 *   "id": 1,
 *   "title": "我的第一个会话",
 *   "modelName": "gpt-4o-mini",
 *   "status": "active",
 *   "lastMessageAt": "2026-07-18 14:30:00",
 *   "createdAt": "2026-07-18 14:00:00"
 * }
 *
 * 字段说明：
 * - id：会话唯一标识
 * - title：会话标题
 * - modelName：使用的 AI 模型
 * - status：会话状态（active / archived）
 * - lastMessageAt：最后一条消息时间（前端用来排序）
 * - createdAt：会话创建时间
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {

    /**
     * 会话 ID
     */
    private Long id;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 使用的 AI 模型名称
     */
    private String modelName;

    /**
     * 会话状态（枚举：active / archived）
     * 通过 @JsonValue 输出小写字符串
     */
    private ConversationStatus status;

    /**
     * 最后一条消息的时间
     * 用 LocalDateTime（前端友好），不是 Instant（绝对时间）
     */
    private LocalDateTime lastMessageAt;

    /**
     * 会话创建时间
     * 用 LocalDateTime（前端友好）
     */
    private LocalDateTime createdAt;
}