package com.keyx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全相关配置
 *
 * - 提供 BCryptPasswordEncoder Bean（供 AuthService 加密密码）
 * - 配置 SecurityFilterChain（Spring Security 过滤规则）
 * - 接入 JwtAuthenticationFilter（解析请求头的 JWT）
 *
 * 路由规则：
 * - /api/auth/**              公开（注册、登录）
 * - /api/chat/stream         需要认证（SSE 流式对话，POST）
 * - /api/chat/abort/**       需要认证（用户停止生成）
 * - /api/chat/**             需要认证（其他 Chat 接口）
 * - 任何其他请求             需要认证
 */
@Configuration
public class SecurityConfig {

    /**
     * JWT 过滤器（需要注入到 SecurityFilterChain）
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * BCrypt 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ① 禁用 CSRF（前后端分离项目不需要）
                .csrf(csrf -> csrf.disable())

                // ② 无状态 Session（用 JWT，不用 Session）
                .sessionManagement(s -> s.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                ))

                // ③ 配置路由权限
                .authorizeHttpRequests(auth -> auth
                        // 公开接口（注册、登录）
                        .requestMatchers("/api/auth/**").permitAll()

                        // 其他所有请求都需要认证
                        // （包括 /api/chat/**，JWT 过滤器会从 header 抠 token）
                        .anyRequest().authenticated()
                )

                // ④ 禁用表单登录
                .formLogin(form -> form.disable())

                // ⑤ 禁用 HTTP Basic
                .httpBasic(basic -> basic.disable())

                // ⑥ 接入 JWT 过滤器（在 UsernamePasswordAuthenticationFilter 之前）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}