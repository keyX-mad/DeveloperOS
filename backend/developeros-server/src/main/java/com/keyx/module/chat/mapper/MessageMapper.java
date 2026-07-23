package com.keyx.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keyx.module.chat.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息 Mapper。
 *
 * 只继承 MyBatis-Plus BaseMapper，查询条件统一在 Service / PromptBuilder 中用 LambdaQueryWrapper 组装。
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
