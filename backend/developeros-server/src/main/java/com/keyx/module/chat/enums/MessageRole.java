package com.keyx.module.chat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 消息角色枚举
 *
 * 对应数据库 message.role 字段，CHECK 约束：
 *   CHECK (role IN ('system', 'user', 'assistant', 'tool'))
 *
 * 枚举值（小写）必须与数据库完全一致
 */
@Getter
public enum MessageRole {

    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    TOOL("tool");

    /**
     * 数据库存储的值（小写字符串）
     * @EnumValue 告诉 MyBatis-Plus 用这个字段存数据库
     * @JsonValue 告诉 Jackson 序列化时用这个值
     */
    @EnumValue
    @JsonValue
    private final String code;

    MessageRole(String code) {
        this.code = code;
    }
}
