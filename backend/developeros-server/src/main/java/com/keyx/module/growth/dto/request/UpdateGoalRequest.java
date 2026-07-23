package com.keyx.module.growth.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 修改学习目标请求 DTO（partial update）
 *
 * 前端发 PUT /api/growth/goals/{id} 时 JSON 长这样（所有字段都可选）：
 * {
 *   "title": "新标题",
 *   "description": "新描述",
 *   "priority": 5,
 *   "status": "active",
 *   "startDate": "2026-07-23",
 *   "targetDate": "2026-12-31"
 * }
 *
 * 设计要点：
 *   1. 所有字段都可选，前端只传要改的字段（PATCH 语义）
 *   2. status 用 String，前端传 "draft" / "active" / "paused" / "completed" / "cancelled"
 *   3. progressPercent 不允许外部修改（由 Service 在 updateStatus 时自动重算）
 *   4. completedAt 不允许外部修改（由 Service 在 complete() 时设置）
 */
@Data
public class UpdateGoalRequest {

    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    private String description;

    @Min(value = 1, message = "优先级必须在 1-5 之间")
    @Max(value = 5, message = "优先级必须在 1-5 之间")
    private Integer priority;

    /**
     * 状态值：draft / active / paused / completed / cancelled
     * 校验在 Service 层做（枚举合法性 + 状态流转合法性）
     */
    private String status;

    private LocalDate startDate;

    @FutureOrPresent(message = "完成日期不能早于今天")
    private LocalDate targetDate;
}
