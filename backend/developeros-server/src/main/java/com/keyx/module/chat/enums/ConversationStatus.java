package com.keyx.module.chat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 会话状态枚举
 *
 * 对应数据库 conversation.status 字段，CHECK 约束：
 *   CHECK (status IN ('active', 'archived'))
 *
 * 枚举值（小写）必须与数据库完全一致
 */
@Getter
public enum ConversationStatus {

    ACTIVE("active"),        // 正常
    ARCHIVED("archived");    // 归档

    @EnumValue
    @JsonValue
    private final String code;

    ConversationStatus(String code) {
        this.code = code;
    }
}
