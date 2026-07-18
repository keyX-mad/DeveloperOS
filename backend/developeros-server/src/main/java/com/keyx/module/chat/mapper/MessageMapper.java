package com.keyx.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keyx.module.chat.entity.Message;
import org.apache.ibatis.annotations.Mapper;

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
 * V1 常用自定义方法（V1.1 再加）：
 * - selectByConversationId(Long conversationId)：查会话的所有消息
 * - selectFirstN(Long conversationId, int n)：查会话前 N 条消息（拼 prompt 用）
 *
 * V1 暂时不加自定义方法，框架先跑通。
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    // V1 暂不加自定义查询方法
}