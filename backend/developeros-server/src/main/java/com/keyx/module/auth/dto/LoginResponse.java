package com.keyx.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 *
 * 后端返回给前端的 JSON 长这样：
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "userId": 1,
 *   "username": "keyx"
 * }
 *
 * 前端拿到后：
 * - token 存到 localStorage
 * - 后续请求 Header: Authorization: Bearer {token}
 * - userId 和 username 可以用来显示
 */
@Data
@Builder          // 让 AuthService 用 builder 模式构造
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT token，前端存起来
     */
    private String token;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;
}