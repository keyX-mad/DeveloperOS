package com.keyx.module.auth.service.impl;

import com.keyx.common.exception.BusinessException;
import com.keyx.module.auth.dto.LoginRequest;
import com.keyx.module.auth.dto.LoginResponse;
import com.keyx.module.auth.dto.RegisterRequest;
import com.keyx.module.auth.service.AuthService;
import com.keyx.module.user.entity.User;
import com.keyx.module.user.service.UserService;
import com.keyx.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest req) {
        // 1. 检查用户名
        if (userService.existsByUsername(req.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查邮箱
        if (userService.existsByEmail(req.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 3. 加密密码
        String passwordHash = passwordEncoder.encode(req.getPassword());

        // 4. 创建 User 对象
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordHash);
        user.setCreatedAt(Instant.now());    // ← 新增：手动设置创建时间
        user.setUpdatedAt(Instant.now());    // ← 新增：手动设置更新时间

        // 5. 保存到数据库
        userService.save(user);

        return user;
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        // 1. 按 username 查用户
        User user = userService.findByUsername(req.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查账号状态
        if (!"active".equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 4. 生成 token
        String token = jwtUtil.generate(user.getId(), user.getUsername());

        // 5. 更新最后登录时间
        user.setLastLoginAt(Instant.now());
        userService.updateById(user);

        // 6. 返回响应
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}