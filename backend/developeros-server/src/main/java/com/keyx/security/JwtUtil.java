package com.keyx.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generate(Long userId, String username) {
        return Jwts.builder()
                .subject(String.valueOf(userId))      // ✅ userId 转字符串
                .claim("username", username)          // ✅ username 放 claim
                .issuedAt(new Date())                  // ✅ 当前时间
                .expiration(new Date(System.currentTimeMillis() + expiration))  // ✅ 用上 expiration
                .signWith(secretKey)                   // ✅ 用缓存的
                .compact();
    }

    public Claims parse(String token) {
        // 返回值：Claims（解析后的用户信息）
       return Jwts.parser()
                .verifyWith(secretKey)                 // ✅ 用缓存的
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validate(String token) {
        try{
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
