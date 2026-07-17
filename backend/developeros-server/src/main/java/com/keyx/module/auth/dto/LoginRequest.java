package com.keyx.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 *
 * 前端发 POST /api/auth/login 时 JSON 长这样：
 * {
 *   "username": "keyx",
 *   "password": "123456"
 * }
 *
 * V1 阶段只支持用户名登录（邮箱登录以后再加）
 */
@Data
public class LoginRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码（明文，Service 层会对比哈希）
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}