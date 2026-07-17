package com.keyx.module.auth.controller;


import com.keyx.common.R;
import com.keyx.module.auth.dto.LoginRequest;
import com.keyx.module.auth.dto.LoginResponse;
import com.keyx.module.auth.dto.RegisterRequest;
import com.keyx.module.auth.service.AuthService;
import com.keyx.module.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public R<User> Register(@Valid @RequestBody RegisterRequest req) {
        User user = authService.register(req);
        return R.ok(user);
    }

    @PostMapping("/login")
    public R<LoginResponse> Login(@Valid @RequestBody LoginRequest req) {
        LoginResponse loginResponse = authService.login(req);
        return R.ok(loginResponse);
    }
}
