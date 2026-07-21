package com.keyx.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keyx.module.chat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会话 Mapper
 *
 * 继承 BaseMapper<Conversation> 自动拥有 CRUD 方法：
 * - insert(Conversation)
 * - updateById(Conversation)
 * - selectById(Long)
 * - selectList(Wrapper)
 * - deleteById(Long)
 * - ...
 *
 * ⚠️ Workaround：MyBatis 3.5.16 + MyBatis-Plus 3.5.5 的 lambdaQuery 在
 * selectList / selectCount 时会触发 OGNL 表达式 'ew.sqlFirst != null'，
 * MyBatis-Plus 的 getSqlFirst() 主动抛异常拒绝。
 * 绕开方案：手写 @Select SQL（不经过 MyBatis-Plus 模板）。
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 手写 SQL：分页查询当前用户的会话
     * 绕开 MyBatis-Plus 的 lambdaQuery OGNL 兼容问题
     * （selectList 会触发 OGNL，所以 records 仍要手写）
     */
    @Select("""
            SELECT id, user_id, title, model_name, status, last_message_at, created_at, updated_at
            FROM conversation
            WHERE user_id = #{userId}
            ORDER BY updated_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<Conversation> selectByUserIdPaged(@Param("userId") Long userId,
                                            @Param("size") int size,
                                            @Param("offset") long offset);
}
