package com.keyx.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keyx.module.chat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话 Mapper。
 *
 * 只继承 MyBatis-Plus BaseMapper，查询条件统一在 Service 层用 LambdaQueryWrapper 组装。
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
