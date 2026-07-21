package com.keyx.common;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Controller 基类
 *
 * 提供当前登录用户 ID 的统一获取入口
 * 所有需要 userId 的 Controller 都应继承本类
 *
 * 前置条件：JwtAuthenticationFilter 已把 Long userId 放进 SecurityContext
 */
public abstract class BaseController {

    /**
     * 获取当前登录用户 ID（Long 类型）
     * 来自 JWT subject，由 JwtAuthenticationFilter 解析后放入 principal
     */
    protected Long currentUserId() {
        return (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
