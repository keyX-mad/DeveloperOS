package com.keyx.module.growth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.module.growth.dto.request.CreateTaskRequest;
import com.keyx.module.growth.dto.request.UpdateTaskRequest;
import com.keyx.module.growth.entity.LearningTask;

import java.util.List;

/**
 * 学习任务 Service 接口
 *
 * 核心约定：
 * - 所有 public 方法第一个参数都是 userId
 * - 必须通过 goalService.getById 间接校验 userId 归属（task → goal → user）
 * - 校验失败统一抛 GrowthException.taskNotFound()（对外 404）
 *
 * 与 GoalService 的依赖关系：
 *   TaskService ──→ GoalService（getById 校验 + recomputeProgress）
 *   GoalService ──→ LearningTaskMapper（recomputeProgress 统计）
 *   单向依赖，无循环
 */
public interface TaskService {

    /**
     * 在目标下创建任务
     * 校验 goal 归属当前用户
     *
     * @param userId 当前登录用户 ID
     * @param goalId 所属目标 ID
     * @param req    创建请求
     * @return 创建好的任务
     */
    LearningTask create(Long userId, Long goalId, CreateTaskRequest req);

    /**
     * 分页查询目标下的所有任务
     * 校验 goal 归属当前用户
     *
     * @param userId  当前登录用户 ID
     * @param goalId  目标 ID
     * @param status  可选状态过滤（todo/in_progress/completed/cancelled）
     * @param current 当前页码（从 1 开始）
     * @param size    每页大小
     * @return 分页结果（按 sort_order ASC, priority DESC, created_at DESC 排序）
     */
    Page<LearningTask> listByGoal(Long userId, Long goalId, String status, int current, int size);

    /**
     * 查询目标下的"今日任务"
     * 定义：scheduled_date = 今天 OR due_at 在今天之内
     * V1 简化：只查 scheduled_date = today
     *
     * @param userId 当前登录用户 ID
     * @param goalId 目标 ID
     * @return 今日任务列表（按 sort_order ASC, priority DESC 排序）
     */
    List<LearningTask> listTodayByGoal(Long userId, Long goalId);

    /**
     * 查询任务详情
     * 通过 task → goal → user 校验归属
     *
     * @param userId 当前登录用户 ID
     * @param taskId 任务 ID
     * @return 任务实体
     */
    LearningTask getById(Long userId, Long taskId);

    /**
     * 更新任务（partial update）
     * 不允许修改 status（请用专用 updateStatus）
     *
     * @param userId 当前登录用户 ID
     * @param taskId 任务 ID
     * @param req    更新请求
     * @return 更新后的任务
     */
    LearningTask update(Long userId, Long taskId, UpdateTaskRequest req);

    /**
     * 修改任务状态 ⭐ 核心方法
     *
     * 事务内执行：
     *   1. 更新 task.status（必要时设置 completedAt）
     *   2. 触发 goalService.recomputeProgress(goalId) 重算目标进度
     *
     * @param userId    当前登录用户 ID
     * @param taskId    任务 ID
     * @param newStatus 新状态（todo/in_progress/completed/cancelled）
     * @return 更新后的任务
     */
    LearningTask updateStatus(Long userId, Long taskId, String newStatus);

    /**
     * 删除任务
     * 删除后触发 goalService.recomputeProgress(goalId) 重算目标进度
     *
     * @param userId 当前登录用户 ID
     * @param taskId 任务 ID
     */
    void delete(Long userId, Long taskId);
}