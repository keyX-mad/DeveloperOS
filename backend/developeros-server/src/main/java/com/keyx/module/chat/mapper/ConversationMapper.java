package com.keyx.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keyx.module.chat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

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
 * 主人要加自定义查询时，在这里声明方法，
 * 然后在 resources/mapper/ConversationMapper.xml 写 SQL。
 *
 * V1 暂时不加自定义方法，框架先跑通。
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    // V1 暂不加自定义查询方法
}