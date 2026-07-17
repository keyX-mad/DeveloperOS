package com.keyx.module.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keyx.module.user.entity.User;
import com.keyx.module.user.mapper.UserMapper;
import com.keyx.module.user.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 Service 实现类
 *
 * 继承 ServiceImpl<UserMapper, User> 自动拥有 CRUD 实现
 * 实现 UserService 接口
 *
 * 主人要写业务方法时（比如按 username 查询），
 * 1. 先在 UserService 接口声明方法
 * 2. 再在这里写实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
                              implements UserService {
    @Override
    public User findByUsername(String username) {

        return lambdaQuery()
                .eq(User::getUsername, username)
                .one();
    }

    @Override
    public User findByEmail(String email) {
        return lambdaQuery()
                .eq(User::getEmail,email)
                .one();
    }

    @Override
    public boolean existsByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .count() > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        return lambdaQuery()
                .eq(User::getEmail, email)
                .count() > 0;
    }


}