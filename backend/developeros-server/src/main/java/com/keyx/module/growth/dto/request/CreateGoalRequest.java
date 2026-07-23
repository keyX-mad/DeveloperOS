package com.keyx.module.growth.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建学习目标请求 DTO
 *
 * 前端发 POST /api/growth/goals 时 JSON 长这样：
 * {
 *   "title": "掌握 Spring Security",
 *   "description": "理解认证授权流程并能完成实战项目",
 *   "priority": 4,
 *   "startDate": "2026-07-23",
 *   "targetDate": "2026-10-23"
 * }
 *
 * status / progressPercent / completedAt 不传，由后端设默认值
 */
@Data
public class CreateGoalRequest {

    /**
     * 目标标题
     * 必填，trim 后不能为空，长度 ≤ 200
     */
    @NotBlank(message = "目标标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    /**
     * 目标描述
     * 可选，TEXT 类型无长度限制（V1 不做服务端长度校验）
     */
    private String description;

    /**
     * 优先级 1-5，越大越重要
     * 可选，默认 3（与数据库一致）
     */
    @Min(value = 1, message = "优先级必须在 1-5 之间")
    @Max(value = 5, message = "优先级必须在 1-5 之间")
    private Integer priority;

    /**
     * 计划开始日期
     * 可选；targetDate 不为空时，DB 会校验 targetDate >= startDate
     */
    private LocalDate startDate;

    /**
     * 计划完成日期
     * 可选；不能早于今天（@FutureOrPresent），由 DB 兜底校验 targetDate >= startDate
     */
    @FutureOrPresent(message = "完成日期不能早于今天")
    private LocalDate targetDate;
}