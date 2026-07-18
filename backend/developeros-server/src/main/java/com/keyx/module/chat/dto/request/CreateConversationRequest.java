package com.keyx.module.chat.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建会话请求 DTO
 *
 * 前端发 POST /api/chat/conversations 时 JSON 长这样：
 * {
 *   "title": "我的第一个会话"   // 可选，不传就用数据库默认值 "New conversation"
 * }
 *
 * @Valid 注解触发 title 的长度校验
 */
@Data
public class CreateConversationRequest {

    /**
     * 会话标题
     * 可选，不传时数据库会用默认值 "New conversation"
     * 长度限制 200 字符（和数据库 VARCHAR(200) 对齐）
     */
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;
}