package com.keyx.module.growth.enums;

import lombok.Getter;

/**
 * 成长日志类型
 *
 * 与数据库 growth_log.log_type 的 CHECK 约束严格一致：
 *   daily / weekly / milestone / reflection / other
 */
@Getter
public enum LogType {
    DAILY("daily", "每日记录"),
    WEEKLY("weekly", "周总结"),
    MILESTONE("milestone", "里程碑"),
    REFLECTION("reflection", "复盘"),
    OTHER("other", "其他");

    private final String code;
    private final String description;

    LogType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static LogType fromCode(String code) {
        if (code == null) return null;
        for (LogType t : values()) {
            if (t.code.equalsIgnoreCase(code)) return t;
        }
        throw new IllegalArgumentException("未知 LogType: " + code);
    }
}