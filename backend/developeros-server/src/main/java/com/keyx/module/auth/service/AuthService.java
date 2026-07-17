package com.keyx.module.auth.service;

import com.keyx.module.auth.dto.LoginRequest;
import com.keyx.module.auth.dto.LoginResponse;
import com.keyx.module.auth.dto.RegisterRequest;
import com.keyx.module.user.entity.User;

public interface AuthService {

    /**
     * 注册用户
     * @return 注册成功的用户
     */
    User register(RegisterRequest req);

    /**
     * 登录
     * @return 登录响应（含 token）
     */
    LoginResponse login(LoginRequest req);
}
