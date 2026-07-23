package com.keyx.module.growth.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 创建学习任务请求 DTO
 *
 * 前端发 POST /api/growth/goals/{goalId}/tasks 时 JSON 长这样：
 * {
 *   "title": "阅读 Spring Security 官方文档第一章",
 *   "description": "理解认证与授权的核心概念",
 *   "priority": 3,
 *   "scheduledDate": "2026-07-25",
 *   "dueAt": "2026-07-30T23:59:59Z",
 *   "sortOrder": 0
 * }
 *
 * goalId 不在这里！它来自 URL 路径
 */
@Data
public class CreateTaskRequest {

    /**
     * 任务标题
     * 必填，trim 后不能为空，长度 ≤ 200
     */
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    /**
     * 任务描述
     * 可选
     */
    private String description;

    @Min(value = 1, message = "优先级必须在 1-5 之间")
    @Max(value = 5, message = "优先级必须在 1-5 之间")
    private Integer priority;

    /**
     * 计划哪一天执行（业务日期）
     * 可空；用于每日任务列表
     */
    private LocalDate scheduledDate;

    /**
     * 最晚完成时间（绝对时间，含时区）
     * 可空；@Future 仅校验晚于当前时刻
     */
    @Future(message = "截止时间必须晚于当前")
    private Instant dueAt;

    /**
     * 同目标下的排序值
     * 可选，默认 0
     */
    @Min(value = 0, message = "排序值不能为负")
    private Integer sortOrder;
}