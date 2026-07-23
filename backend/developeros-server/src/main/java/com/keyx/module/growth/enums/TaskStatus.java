package com.keyx.module.growth.enums;

import lombok.Getter;

/**
 * 学习任务状态
 *
 * 与数据库 learning_task.status 的 CHECK 约束严格一致：
 *   todo / in_progress / completed / cancelled
 */
@Getter
public enum TaskStatus {
    TODO("todo", "待办"),
    IN_PROGRESS("in_progress", "进行中"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String description;

    TaskStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TaskStatus fromCode(String code) {
        if (code == null) return null;
        for (TaskStatus s : values()) {
            if (s.code.equalsIgnoreCase(code)) return s;
        }
        throw new IllegalArgumentException("未知 TaskStatus: " + code);
    }
}