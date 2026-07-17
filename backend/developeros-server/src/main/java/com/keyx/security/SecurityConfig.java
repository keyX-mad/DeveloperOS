package com.keyx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全相关配置
 * <p>
 * 这里集中注册安全相关的 Bean，避免散落在各个业务模块中。
 * 当前主要用于提供密码编码器，供 AuthService 等模块做密码加密与校验。
 */
@Configuration
public class SecurityConfig {

    /**
     * BCrypt 密码编码器
     * <p>
     * BCrypt 是 Spring Security 官方推荐的密码哈希算法：
     * - 单向加密（不可逆）
     * - 自动加盐（同一个密码每次加密结果都不同）
     * - 可调强度（默认 cost = 10）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())                          // ① 禁用 CSRF
                .sessionManagement(s -> s.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS                    // ② 无状态 Session
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()        // ③ 放行 Auth 接口
                        .anyRequest().authenticated()                       // ④ 其他接口需要认证
                )
                .formLogin(form -> form.disable())                     // ⑤ 禁用表单登录
                .httpBasic(basic -> basic.disable());                   // ⑥ 禁用 HTTP Basic

        return http.build();
    }


}
