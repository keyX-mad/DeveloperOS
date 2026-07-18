package com.keyx.module.chat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 消息状态枚举
 *
 * 对应数据库 message.status 字段，CHECK 约束：
 *   CHECK (status IN ('streaming', 'completed', 'failed', 'stopped'))
 */
@Getter
public enum MessageStatus {

    STREAMING("streaming"),     // 流式生成中
    COMPLETED("completed"),     // 生成完成
    FAILED("failed"),           // 生成失败
    STOPPED("stopped");          // 用户/系统中止

    @EnumValue
    @JsonValue
    private final String code;

    MessageStatus(String code) {
        this.code = code;
    }
}