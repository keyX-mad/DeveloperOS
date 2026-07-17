package com.keyx.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keyx.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * 继承 BaseMapper<User> 后自动拥有 CRUD 方法：
 * - insert(User)
 * - updateById(User)
 * - selectById(Long)
 * - selectList(Wrapper)
 * - deleteById(Long)
 * - ... 更多
 *
 * 主人要加自定义查询时，在这个接口写方法，
 * 然后在 resources/mapper/UserMapper.xml 写 SQL。
 *
 * ⚠️ V1 暂时不加自定义方法，框架先跑通
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 主人之后会在这里加自定义查询方法
    // 例如：
    // User selectByUsername(String username);
    // User selectByEmail(String email);
    // boolean existsByUsername(String username);
}