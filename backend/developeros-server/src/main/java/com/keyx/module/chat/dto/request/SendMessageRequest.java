package com.keyx.module.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送消息请求 DTO
 *
 * 前端发 POST /api/chat/conversations/{id}/messages 时 JSON 长这样：
 * {
 *   "content": "你好，介绍一下 Spring Boot",   // 必填
 *   "modelName": "gpt-4o-mini"                 // 可选，不传就用用户默认模型
 * }
 */
@Data
public class SendMessageRequest {

    /**
     * 消息内容
     * 必填：trim 后不能为空
     * 长度限制 10000 字符（V1 临时值，后续可调）
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 10000, message = "消息内容过长")
    private String content;

    /**
     * 使用的模型名称
     * 可选，不传时：
     *   1. 用当前 conversation 的 modelName
     *   2. 如果 conversation 也没有，用全局默认（application.yml 配置）
     */
    private String modelName;
}