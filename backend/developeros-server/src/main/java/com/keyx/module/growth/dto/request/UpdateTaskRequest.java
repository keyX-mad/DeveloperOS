package com.keyx.module.growth.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 修改学习任务请求 DTO（partial update）
 *
 * 前端发 PUT /api/growth/tasks/{id} 时 JSON 长这样（所有字段都可选）：
 * {
 *   "title": "新标题",
 *   "description": "新描述",
 *   "priority": 4,
 *   "scheduledDate": "2026-07-26",
 *   "dueAt": "2026-07-31T23:59:59Z",
 *   "sortOrder": 1
 * }
 *
 * 注意：status 修改请用专门的 UpdateTaskStatusRequest，
 *      因为改状态会触发进度重算，逻辑不一样
 */
@Data
public class UpdateTaskRequest {

    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    private String description;

    @Min(value = 1, message = "优先级必须在 1-5 之间")
    @Max(value = 5, message = "优先级必须在 1-5 之间")
    private Integer priority;

    private LocalDate scheduledDate;

    @Future(message = "截止时间必须晚于当前")
    private Instant dueAt;

    @Min(value = 0, message = "排序值不能为负")
    private Integer sortOrder;
}