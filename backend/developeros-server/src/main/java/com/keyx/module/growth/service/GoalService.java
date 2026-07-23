package com.keyx.module.growth.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.module.growth.dto.request.CreateGoalRequest;
import com.keyx.module.growth.dto.request.UpdateGoalRequest;
import com.keyx.module.growth.entity.LearningGoal;

/**
 * 学习目标 Service 接口
 *
 * 核心约定：
 * - 所有 public 方法第一个参数都是 userId（当前登录用户 ID）
 * - 实现类内部必须做 userId 归属校验，防止越权
 * - 校验失败统一抛 GrowthException 1701（对外表现为 404），防止信息泄露
 *
 * 内部方法（包内/同模块可见）不需要 userId 参数：
 *   - recomputeProgress(goalId) 只供 TaskService 调用
 */
public interface GoalService {

    /**
     * 创建学习目标
     *
     * @param userId 当前登录用户 ID
     * @param req    创建请求
     * @return 创建好的目标（已含 DB 自动生成的 id / createdAt / updatedAt）
     */
    LearningGoal create(Long userId, CreateGoalRequest req);

    /**
     * 分页查询当前用户的目标
     *
     * @param userId  当前登录用户 ID
     * @param status  可选的状态过滤（draft/active/paused/completed/cancelled）
     * @param current 当前页码（从 1 开始）
     * @param size    每页大小
     * @return 分页结果（按 target_date ASC NULLS LAST, priority DESC, created_at DESC 排序）
     */
    Page<LearningGoal> list(Long userId, String status, int current, int size);

    /**
     * 查询目标详情
     * 实现类内部必须校验目标归属当前用户
     *
     * @param userId 当前登录用户 ID
     * @param goalId 目标 ID
     * @return 目标实体
     * @throws com.keyx.module.growth.exception.GrowthException 1701 当目标不存在或不属于当前用户
     */
    LearningGoal getById(Long userId, Long goalId);

    /**
     * 更新目标（partial update）
     * 实现类内部必须校验目标归属当前用户
     * 不允许外部修改 progressPercent 和 completedAt（由 Service 内部维护）
     *
     * @param userId 当前登录用户 ID
     * @param goalId 目标 ID
     * @param req    更新请求
     * @return 更新后的目标
     */
    LearningGoal update(Long userId, Long goalId, UpdateGoalRequest req);

    /**
     * 删除目标（数据库级联删除 tasks；关联 logs 的 goal_id 会被置空）
     * 实现类内部必须校验目标归属当前用户
     *
     * @param userId 当前登录用户 ID
     * @param goalId 目标 ID
     */
    void delete(Long userId, Long goalId);

    /**
     * 标记目标完成
     * - status → COMPLETED
     * - progressPercent → 100
     * - completedAt → now()
     *
     * 实现类内部必须校验目标归属当前用户
     *
     * @param userId 当前登录用户 ID
     * @param goalId 目标 ID
     * @return 更新后的目标
     */
    LearningGoal complete(Long userId, Long goalId);

    // ============================================
    // 内部方法（仅供 TaskService 调用，避免双向耦合）
    // ============================================

    /**
     * 重算目标进度（progressPercent）
     *
     * 算法：已完成任务数 / 未取消任务总数 * 100（四舍五入）
     * - 任务总数为 0 → 进度 0
     * - 已完成 = 任务总数 → 进度 100 且自动填 completedAt（仅当 status=ACTIVE 时）
     *
     * 仅供 TaskService 在事务内调用。
     * 调用方应已校验过 userId 归属，这里不再重复校验。
     *
     * @param goalId 目标 ID
     */
    void recomputeProgress(Long goalId);
}
