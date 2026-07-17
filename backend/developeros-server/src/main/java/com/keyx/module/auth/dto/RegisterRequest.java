package com.keyx.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 DTO
 *
 * 前端发 POST /api/auth/register 时 JSON 长这样：
 * {
 *   "username": "keyx",
 *   "email": "keyx@example.com",
 *   "password": "123456"
 * }
 *
 * @Valid 注解触发校验，校验失败时
 * GlobalExceptionHandler 会自动捕获并返回错误信息
 */
@Data
public class RegisterRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在 3-50 之间")
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码（明文，Service 层会加密存储）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 之间")
    private String password;
}