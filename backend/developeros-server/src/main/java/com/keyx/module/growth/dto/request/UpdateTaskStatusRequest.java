package com.keyx.module.growth.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改任务状态请求 DTO（专用）
 *
 * 前端发 PATCH /api/growth/tasks/{id}/status 时 JSON 长这样：
 * {
 *   "status": "in_progress"   // todo / in_progress / completed / cancelled
 * }
 *
 * 为什么单独做：
 *   1. 状态变更 → 自动触发 goal.progressPercent 重算 → 原子事务
 *   2. 进入 completed → 自动填 completedAt
 *   3. 进入 cancelled → 不算入分母
 *   4. 业务边界清晰，Controller 调用专门 Service 方法
 */
@Data
public class UpdateTaskStatusRequest {

    /**
     * 目标状态
     * todo / in_progress / completed / cancelled
     * Service 层用 TaskStatus.fromCode() 校验合法性
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}