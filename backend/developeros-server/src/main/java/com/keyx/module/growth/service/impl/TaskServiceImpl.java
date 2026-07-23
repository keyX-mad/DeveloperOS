package com.keyx.module.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keyx.module.growth.dto.request.CreateTaskRequest;
import com.keyx.module.growth.dto.request.UpdateTaskRequest;
import com.keyx.module.growth.entity.LearningTask;
import com.keyx.module.growth.enums.TaskStatus;
import com.keyx.module.growth.exception.GrowthException;
import com.keyx.module.growth.mapper.LearningTaskMapper;
import com.keyx.module.growth.service.GoalService;
import com.keyx.module.growth.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 学习任务 Service 实现
 *
 * 核心安全要求：所有 public 方法都必须先通过 goalService.getById 间接校验 userId 归属！
 *
 * 关键设计：
 * - create / list / getById / update / updateStatus / delete 都在事务边界内
 * - updateStatus ⭐ 是 Growth 模块最核心的方法：
 *   改 task 状态 + 同步重算 goal 进度，必须在同一事务
 * - 列表查询统一用 Wrappers.lambdaQuery(LearningTask.class) 绕开 OGNL
 */
@Service
public class TaskServiceImpl extends ServiceImpl<LearningTaskMapper, LearningTask> implements TaskService {

    @Autowired
    private LearningTaskMapper learningTaskMapper;

    /**
     * 注入 GoalService：
     *   1. 用 goalService.getById(userId, goalId) 校验 task 归属
     *   2. 状态变更后调 goalService.recomputeProgress(goalId) 重算进度
     *
     * 单向依赖：TaskService → GoalService → LearningTaskMapper
     * 不构成循环
     */
    @Autowired
    private GoalService goalService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LearningTask create(Long userId, Long goalId, CreateTaskRequest req) {
        // 1. 校验 goal 归属当前用户（不存在/不属于 → 抛 404）
        goalService.getById(userId, goalId);

        // 2. 构建实体
        Instant now = Instant.now();
        LearningTask task = new LearningTask();
        task.setGoalId(goalId);
        task.setTitle(req.getTitle().trim());
        task.setDescription(req.getDescription());
        task.setStatus(TaskStatus.TODO.getCode());  // 默认 todo
        task.setPriority(req.getPriority() != null ? req.getPriority() : 3);
        task.setScheduledDate(req.getScheduledDate());
        task.setDueAt(req.getDueAt());
        task.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);

        // 手动 set 时间（MyMetaObjectHandler 暂未生效）
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        // 3. 持久化
        learningTaskMapper.insert(task);

        // 4. 创建新 task 不需要重算（默认 todo，进度不变）
        return task;
    }

    @Override
    public Page<LearningTask> listByGoal(Long userId, Long goalId, String status, int current, int size) {
        // 1. 校验 goal 归属当前用户
        goalService.getById(userId, goalId);

        // 2. 构造查询
        Page<LearningTask> page = new Page<>(current, size);
        LambdaQueryWrapper<LearningTask> wrapper = Wrappers.lambdaQuery(LearningTask.class)
                .eq(LearningTask::getGoalId, goalId)
                .orderByAsc(LearningTask::getSortOrder)
                .orderByDesc(LearningTask::getPriority)
                .orderByDesc(LearningTask::getCreatedAt);

        // 3. 可选状态过滤
        if (status != null && !status.isBlank()) {
            TaskStatus.fromCode(status);  // 顺便校验合法性
            wrapper.eq(LearningTask::getStatus, status);
        }

        return learningTaskMapper.selectPage(page, wrapper);
    }

    /**
     * 今日任务：scheduled_date = 今天
     * V1 简化：忽略 due_at（V2 可扩展 due_at 在今天之内）
     */
    @Override
    public List<LearningTask> listTodayByGoal(Long userId, Long goalId) {
        // 1. 校验 goal 归属
        goalService.getById(userId, goalId);

        // 2. 查今日
        LocalDate today = LocalDate.now();
        return learningTaskMapper.selectList(
                Wrappers.lambdaQuery(LearningTask.class)
                        .eq(LearningTask::getGoalId, goalId)
                        .eq(LearningTask::getScheduledDate, today)
                        // 只查未完成的任务（cancelled 也不算）
                        .in(LearningTask::getStatus,
                                List.of(TaskStatus.TODO.getCode(), TaskStatus.IN_PROGRESS.getCode()))
                        .orderByAsc(LearningTask::getSortOrder)
                        .orderByDesc(LearningTask::getPriority));
    }

    /**
     * 查询任务详情（带 userId 校验）
     * 校验链路：task → goal → userId
     */
    @Override
    public LearningTask getById(Long userId, Long taskId) {
        // 1. 查 task
        LearningTask task = learningTaskMapper.selectById(taskId);

        // 2. null 检查（防御）
        if (task == null) {
            throw GrowthException.taskNotFound();
        }

        // 3. 通过 goalService.getById 间接校验 userId 归属
        //    如果 goal 不存在/不属于当前用户 → 抛 GrowthException.goalNotFound() (1701)
        //    但我们想抛 taskNotFound() (1702)，需要 catch 并转换
        try {
            goalService.getById(userId, task.getGoalId());
        } catch (GrowthException e) {
            // goal 不属于当前用户 → 对外表现"task 不存在"
            throw GrowthException.taskNotFound();
        }

        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LearningTask update(Long userId, Long taskId, UpdateTaskRequest req) {
        // 1. 校验归属（getById 内部已做）
        LearningTask task = getById(userId, taskId);

        // 2. partial update（不改 status）
        if (req.getTitle() != null) {
            task.setTitle(req.getTitle().trim());
        }
        if (req.getDescription() != null) {
            task.setDescription(req.getDescription());
        }
        if (req.getPriority() != null) {
            task.setPriority(req.getPriority());
        }
        if (req.getScheduledDate() != null) {
            task.setScheduledDate(req.getScheduledDate());
        }
        if (req.getDueAt() != null) {
            task.setDueAt(req.getDueAt());
        }
        if (req.getSortOrder() != null) {
            task.setSortOrder(req.getSortOrder());
        }

        // 3. 持久化
        learningTaskMapper.updateById(task);
        return task;
    }

    /**
     * ⭐⭐⭐ updateStatus - Growth 模块最核心方法
     *
     * 事务内：
     *   1. 更新 task.status
     *   2. 根据新状态维护 completedAt
     *   3. 触发 goalService.recomputeProgress(goalId) 重算目标进度
     *
     * 状态流转规则：
     *   TODO → IN_PROGRESS / COMPLETED / CANCELLED
     *   IN_PROGRESS → COMPLETED / CANCELLED / TODO
     *   COMPLETED → (终态，V1 不允许再改)
     *   CANCELLED → TODO（允许"复活"）
     *
     * completedAt 维护：
     *   - COMPLETED → set completedAt = now
     *   - 其他 → 不变（保留历史）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LearningTask updateStatus(Long userId, Long taskId, String newStatusCode) {
        // 1. 校验归属 + 拿到 task
        LearningTask task = getById(userId, taskId);

        // 2. 解析 + 校验新状态合法性
        TaskStatus newStatus = TaskStatus.fromCode(newStatusCode);

        // 3. 状态流转合法性校验
        validateStatusTransition(task.getStatus(), newStatus);

        // 4. 更新状态
        task.setStatus(newStatus.getCode());

        // 5. 维护 completedAt
        if (newStatus == TaskStatus.COMPLETED) {
            // 完成 → 填 completedAt（如果还没填）
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(Instant.now());
            }
        }
        // 其他状态不修改 completedAt（保留历史）

        // 6. 持久化
        learningTaskMapper.updateById(task);

        // 7. ⭐ 触发目标进度重算（同一事务）
        goalService.recomputeProgress(task.getGoalId());

        return task;
    }

    /**
     * 删除任务
     * 删除后触发重算进度（因为分母变了）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long taskId) {
        // 1. 校验归属 + 拿到 task（需要 goalId）
        LearningTask task = getById(userId, taskId);
        Long goalId = task.getGoalId();

        // 2. 删除
        learningTaskMapper.deleteById(taskId);

        // 3. ⭐ 重算目标进度（同一事务）
        goalService.recomputeProgress(goalId);
    }

    // ============================================
    // 私有方法：状态流转校验
    // ============================================

    /**
     * 任务状态流转校验
     *
     * 允许的流转：
     *   TODO        → IN_PROGRESS / COMPLETED / CANCELLED
     *   IN_PROGRESS → COMPLETED / CANCELLED / TODO
     *   COMPLETED   → (终态，V1 不允许再改)
     *   CANCELLED   → TODO（允许"复活"为待办）
     *
     * 同状态允许（幂等）。
     */
    private void validateStatusTransition(String fromCode, TaskStatus to) {
        if (fromCode == null) return;
        TaskStatus from = TaskStatus.fromCode(fromCode);

        // 同状态（幂等）
        if (from == to) return;

        // 终态校验
        if (from == TaskStatus.COMPLETED) {
            throw GrowthException.invalidStatusTransition(
                    "任务已完成，不能再变更状态");
        }

        // 流转校验
        boolean allowed = switch (from) {
            case TODO -> to == TaskStatus.IN_PROGRESS
                    || to == TaskStatus.COMPLETED
                    || to == TaskStatus.CANCELLED;
            case IN_PROGRESS -> to == TaskStatus.COMPLETED
                    || to == TaskStatus.CANCELLED
                    || to == TaskStatus.TODO;
            case CANCELLED -> to == TaskStatus.TODO;  // 允许"复活"
            default -> false;
        };

        if (!allowed) {
            throw GrowthException.invalidStatusTransition(
                    "不能从 " + from.getDescription() + " 流转到 " + to.getDescription());
        }
    }
}