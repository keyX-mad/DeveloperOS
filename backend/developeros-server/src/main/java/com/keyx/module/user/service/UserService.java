package com.keyx.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keyx.module.user.entity.User;

/**
 * 用户 Service 接口
 *
 * 继承 IService<User> 后接口自动拥有：
 * - save(User)
 * - updateById(User)
 * - getById(Long)
 * - list()
 * - removeById(Long)
 * - ... 更多
 *
 * 自定义业务方法在这里声明，实现在 UserServiceImpl。
 */
public interface UserService extends IService<User> {

    // 主人之后在这里声明自定义业务方法
    // 例如：
    // User findByUsername(String username);
    // User findByEmail(String email);
    // boolean existsByUsername(String username);
    
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}