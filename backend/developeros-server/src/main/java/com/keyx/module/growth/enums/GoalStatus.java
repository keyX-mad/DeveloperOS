package com.keyx.module.growth.enums;

import lombok.Getter;

/**
 * 学习目标状态
 *
 * 与数据库 learning_goal.status 的 CHECK 约束严格一致：
 *   draft / active / paused / completed / cancelled
 *
 * 状态流转：
 *   draft → active → paused → active → completed
 *                          ↘ cancelled
 *                  ↘ cancelled
 */
@Getter
public enum GoalStatus {
    DRAFT("draft", "草稿"),
    ACTIVE("active", "进行中"),
    PAUSED("paused", "已暂停"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String description;

    GoalStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 按 code 解析（数据库 → Java）
     */
    public static GoalStatus fromCode(String code) {
        if (code == null) return null;
        for (GoalStatus s : values()) {
            if (s.code.equalsIgnoreCase(code)) return s;
        }
        throw new IllegalArgumentException("未知 GoalStatus: " + code);
    }
}