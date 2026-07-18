package com.keyx.module.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新会话请求 DTO
 *
 * 前端发 PUT /api/chat/conversations/{id} 时 JSON 长这样：
 * {
 *   "title": "新标题"   // 必填
 * }
 *
 * @Valid 注解触发 title 的非空和长度校验
 */
@Data
public class UpdateConversationRequest {

    /**
     * 新的会话标题
     * 必填：不能是空字符串、纯空格或 null
     * 长度限制 200 字符
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;
}