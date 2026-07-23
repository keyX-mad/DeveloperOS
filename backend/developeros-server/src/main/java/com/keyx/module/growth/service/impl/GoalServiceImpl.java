package com.keyx.module.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keyx.module.growth.dto.request.CreateGoalRequest;
import com.keyx.module.growth.dto.request.UpdateGoalRequest;
import com.keyx.module.growth.entity.LearningGoal;
import com.keyx.module.growth.enums.GoalStatus;
import com.keyx.module.growth.exception.GrowthException;
import com.keyx.module.growth.mapper.LearningGoalMapper;
import com.keyx.module.growth.mapper.LearningTaskMapper;
import com.keyx.module.growth.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 学习目标 Service 实现
 *
 * 核心安全要求：所有 public 方法都必须先校验 userId 归属！
 * - 防止 A 用户访问/修改/删除 B 用户的目标
 * - 校验失败统一抛 GrowthException.goalNotFound()（对外 404），防止信息泄露
 *
 * 关键设计：
 * - 列表查询统一用 Wrappers.lambdaQuery(LearningGoal.class) 走 selectPage（绕开 OGNL）
 * - 时间字段手动 set（MyMetaObjectHandler 暂未生效）
 * - recomputeProgress 暴露给 TaskService 调用，事务内重算进度
 */
@Service
public class GoalServiceImpl extends ServiceImpl<LearningGoalMapper, LearningGoal> implements GoalService {

    @Autowired
    private LearningGoalMapper learningGoalMapper;

    /**
     * 注入 LearningTaskMapper 用于 recomputeProgress 统计任务数
     * 同模块内互相依赖可以接受（不构成循环）
     */
    @Autowired
    private LearningTaskMapper learningTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LearningGoal create(Long userId, CreateGoalRequest req) {
        Instant now = Instant.now();

        LearningGoal goal = new LearningGoal();
        goal.setUserId(userId);
        goal.setTitle(req.getTitle().trim());      // 手动 trim（数据库 CHECK 只校验 btrim）
        goal.setDescription(req.getDescription());
        goal.setStatus(GoalStatus.DRAFT.getCode()); // 默认 draft，让用户主动改为 active
        goal.setPriority(req.getPriority() != null ? req.getPriority() : 3);  // 默认 3
        goal.setProgressPercent(0);                  // 初始 0
        goal.setStartDate(req.getStartDate());
        goal.setTargetDate(req.getTargetDate());

        // 手动 set 时间（MyMetaObjectHandler 暂未生效）
        goal.setCreatedAt(now);
        goal.setUpdatedAt(now);

        learningGoalMapper.insert(goal);
        return goal;
    }

    @Override
    public Page<LearningGoal> list(Long userId, String status, int current, int size) {
        Page<LearningGoal> page = new Page<>(current, size);

        // ✅ 关键：用 Wrappers.lambdaQuery(Class) 走 mapper.selectPage，绕开 OGNL
        LambdaQueryWrapper<LearningGoal> wrapper = Wrappers.lambdaQuery(LearningGoal.class)
                .eq(LearningGoal::getUserId, userId)
                .orderByAsc(LearningGoal::getTargetDate)  // 即将到期的在前
                .orderByDesc(LearningGoal::getPriority)
                .orderByDesc(LearningGoal::getCreatedAt);

        // 可选状态过滤
        if (status != null && !status.isBlank()) {
            // 顺便校验合法性（防止用户传乱七八糟的字符串）
            GoalStatus.fromCode(status);  // 不合法会抛 IllegalArgumentException
            wrapper.eq(LearningGoal::getStatus, status);
        }

        return learningGoalMapper.selectPage(page, wrapper);
    }

    /**
     * 查询目标详情（带 userId 校验）
     */
    @Override
    public LearningGoal getById(Long userId, Long goalId) {
        LearningGoal goal = learningGoalMapper.selectById(goalId);

        // null + userId 归属校验（任一失败抛 1701，对外 404）
        if (goal == null || !goal.getUserId().equals(userId)) {
            throw GrowthException.goalNotFound();
        }
        return goal;
    }

    /**
     * 更新目标（partial update）
     * 注意：不允许修改 progressPercent 和 completedAt
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LearningGoal update(Long userId, Long goalId, UpdateGoalRequest req) {
        // 1. 校验归属（getById 内部已做）
        LearningGoal goal = getById(userId, goalId);

        // 2. 只更新非空字段（partial update）
        if (req.getTitle() != null) {
            goal.setTitle(req.getTitle().trim());
        }
        if (req.getDescription() != null) {
            goal.setDescription(req.getDescription());
        }
        if (req.getPriority() != null) {
            goal.setPriority(req.getPriority());
        }
        if (req.getStatus() != null) {
            // 状态合法性校验 + 流转合法性校验
            GoalStatus newStatus = GoalStatus.fromCode(req.getStatus());
            validateStatusTransition(goal.getStatus(), newStatus);
            goal.setStatus(newStatus.getCode());
        }
        if (req.getStartDate() != null) {
            goal.setStartDate(req.getStartDate());
        }
        if (req.getTargetDate() != null) {
            goal.setTargetDate(req.getTargetDate());
        }

        // 3. 持久化（DB 触发器自动维护 updated_at）
        learningGoalMapper.updateById(goal);

        return goal;
    }

    /**
     * 删除目标
     * - 数据库 CASCADE 自动删除 tasks
     * - 数据库 SET NULL 把关联 logs 的 goal_id 置空（保留日志本身）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long goalId) {
        // 1. 校验归属（getById 内部已做）
        getById(userId, goalId);

        // 2. 删除（CASCADE + SET NULL 由 DB 处理）
        learningGoalMapper.deleteById(goalId);
    }

    /**
     * 标记目标完成
     * 不重算进度，直接设 100（因为是手动标记，覆盖原进度）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LearningGoal complete(Long userId, Long goalId) {
        // 1. 校验归属
        LearningGoal goal = getById(userId, goalId);

        // 2. 状态更新
        goal.setStatus(GoalStatus.COMPLETED.getCode());
        goal.setProgressPercent(100);
        goal.setCompletedAt(Instant.now());

        learningGoalMapper.updateById(goal);
        return goal;
    }

    // ============================================
    // 内部方法：进度重算（仅供 TaskService 调用）
    // ============================================

    /**
     * 重算目标进度
     *
     * 算法：
     *   进度 = 已完成任务数 / (总任务数 - 已取消任务数) * 100
     *
     * 边界：
     *   - 没有任务 → 进度 0
     *   - 全部完成且原状态是 active → 自动填 completedAt
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recomputeProgress(Long goalId) {
        // 1. 统计有效任务数（排除 cancelled）
        Long total = learningTaskMapper.selectCount(
                Wrappers.lambdaQuery(com.keyx.module.growth.entity.LearningTask.class)
                        .eq(com.keyx.module.growth.entity.LearningTask::getGoalId, goalId)
                        .ne(com.keyx.module.growth.entity.LearningTask::getStatus,
                                com.keyx.module.growth.enums.TaskStatus.CANCELLED.getCode()));

        // 2. 边界：没有任务 → 进度 0
        if (total == null || total == 0) {
            updateProgressOnly(goalId, 0);
            return;
        }

        // 3. 统计已完成任务数
        Long completed = learningTaskMapper.selectCount(
                Wrappers.lambdaQuery(com.keyx.module.growth.entity.LearningTask.class)
                        .eq(com.keyx.module.growth.entity.LearningTask::getGoalId, goalId)
                        .eq(com.keyx.module.growth.entity.LearningTask::getStatus,
                                com.keyx.module.growth.enums.TaskStatus.COMPLETED.getCode()));

        // 4. 计算百分比（四舍五入）
        int percent = (int) Math.round(completed * 100.0 / total);

        // 5. 更新进度
        updateProgressOnly(goalId, percent);

        // 6. 全员完成且原状态是 active → 自动标记目标完成
        if (percent == 100) {
            LearningGoal goal = learningGoalMapper.selectById(goalId);
            if (goal != null && GoalStatus.ACTIVE.getCode().equals(goal.getStatus())
                    && goal.getCompletedAt() == null) {
                goal.setStatus(GoalStatus.COMPLETED.getCode());
                goal.setCompletedAt(Instant.now());
                learningGoalMapper.updateById(goal);
            }
        }
    }

    /**
     * 仅更新 progress_percent（不触发其他副作用）
     */
    private void updateProgressOnly(Long goalId, int percent) {
        LearningGoal goal = learningGoalMapper.selectById(goalId);
        if (goal == null) return;  // 防御：goal 不存在就不更新
        goal.setProgressPercent(percent);
        learningGoalMapper.updateById(goal);
    }

    // ============================================
    // 私有方法：状态流转校验
    // ============================================

    /**
     * 校验状态流转合法性
     * 允许的流转：
     *   draft → active / cancelled
     *   active → paused / completed / cancelled
     *   paused → active / cancelled
     *   completed → (终态)
     *   cancelled → (终态)
     */
    private void validateStatusTransition(String fromCode, GoalStatus to) {
        if (fromCode == null) return;
        GoalStatus from = GoalStatus.fromCode(fromCode);

        // 终态不能流转
        if (from == GoalStatus.COMPLETED || from == GoalStatus.CANCELLED) {
            throw GrowthException.invalidStatusTransition(
                    "目标已完成/取消，不能再变更状态（当前：" + from.getDescription() + "）");
        }

        // 同状态允许（幂等）
        if (from == to) return;

        // 检查允许的流转
        boolean allowed = switch (from) {
            case DRAFT -> to == GoalStatus.ACTIVE || to == GoalStatus.CANCELLED;
            case ACTIVE -> to == GoalStatus.PAUSED || to == GoalStatus.COMPLETED || to == GoalStatus.CANCELLED;
            case PAUSED -> to == GoalStatus.ACTIVE || to == GoalStatus.CANCELLED;
            default -> false;
        };

        if (!allowed) {
            throw GrowthException.invalidStatusTransition(
                    "不能从 " + from.getDescription() + " 流转到 " + to.getDescription());
        }
    }
}