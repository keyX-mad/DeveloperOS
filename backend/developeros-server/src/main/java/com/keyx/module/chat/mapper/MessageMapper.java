package com.keyx.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keyx.module.chat.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息 Mapper
 *
 * 继承 BaseMapper<Message> 自动拥有 CRUD 方法：
 * - insert(Message)
 * - updateById(Message)
 * - selectById(Long)
 * - selectList(Wrapper)
 * - deleteById(Long)
 * - ...
 *
 * ⚠️ Workaround：手写 @Select SQL 绕开 MyBatis-Plus 3.5.5 + MyBatis 3.5.16 OGNL 兼容问题
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 手写 SQL：分页查会话的已完成消息（按时间正序）
     * 绕开 MyBatis-Plus 的 lambdaQuery OGNL 兼容问题
     */
    @Select("""
            SELECT id, conversation_id, role, content, status, model_name, token_count,
                   error_message, metadata, created_at, updated_at
            FROM message
            WHERE conversation_id = #{conversationId}
              AND status = 'completed'
            ORDER BY created_at ASC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<Message> selectByConversationPaged(@Param("conversationId") Long conversationId,
                                            @Param("size") int size,
                                            @Param("offset") long offset);

    /**
     * 手写 SQL：取会话最近 N 条已完成消息（按时间倒序）
     * 用于 PromptBuilder 组装上下文（链式 .list() 触发 OGNL，只能手写）
     */
    @Select("""
            SELECT id, conversation_id, role, content, status, model_name, token_count,
                   error_message, metadata, created_at, updated_at
            FROM message
            WHERE conversation_id = #{conversationId}
              AND status = 'completed'
            ORDER BY created_at DESC
            LIMIT #{limit}
            """)
    List<Message> selectRecentCompletedByConversation(@Param("conversationId") Long conversationId,
                                                       @Param("limit") int limit);
}
