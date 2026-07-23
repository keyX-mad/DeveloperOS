package com.keyx.module.growth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.module.growth.dto.request.CreateGrowthLogRequest;
import com.keyx.module.growth.dto.request.UpdateGrowthLogRequest;
import com.keyx.module.growth.entity.GrowthLog;

import java.util.List;

/**
 * 成长日志 Service 接口
 *
 * 核心约定：
 * - 所有 public 方法第一个参数都是 userId
 * - 必须校验 userId 归属
 * - 创建日志时如果关联 goal，必须校验 goal 属于当前用户（复合外键保护）
 * - 校验失败统一抛 GrowthException.logNotFound()（对外 404）
 *
 * 与 GoalService 的依赖关系：
 *   GrowthLogService ──→ GoalService（仅 getById 校验）
 *   单向依赖，无循环
 */
public interface GrowthLogService {

    /**
     * 创建成长日志
     *
     * 如果 req.goalId 不为空，会校验该目标属于当前用户
     * （数据库复合外键 fk_growth_log_goal_user 会兜底校验，但 Service 主动校验给友好错误）
     *
     * @param userId 当前登录用户 ID
     * @param req    创建请求
     * @return 创建好的日志
     */
    GrowthLog create(Long userId, CreateGrowthLogRequest req);

    /**
     * 分页查询当前用户的日志
     *
     * @param userId   当前登录用户 ID
     * @param logType  可选日志类型过滤（daily/weekly/milestone/reflection/other）
     * @param current  当前页码（从 1 开始）
     * @param size     每页大小
     * @return 分页结果（按 log_date DESC, id DESC 排序）
     */
    Page<GrowthLog> list(Long userId, String logType, int current, int size);

    /**
     * 查询日志详情
     *
     * @param userId 当前登录用户 ID
     * @param logId  日志 ID
     * @return 日志实体
     */
    GrowthLog getById(Long userId, Long logId);

    /**
     * 更新日志（partial update）
     * 不允许修改 goalId（避免日志脱离原目标上下文）
     *
     * @param userId 当前登录用户 ID
     * @param logId  日志 ID
     * @param req    更新请求
     * @return 更新后的日志
     */
    GrowthLog update(Long userId, Long logId, UpdateGrowthLogRequest req);

    /**
     * 删除日志
     * 物理删除（V1 简化方案）
     *
     * @param userId 当前登录用户 ID
     * @param logId  日志 ID
     */
    void delete(Long userId, Long logId);

    /**
     * 查询目标关联的所有日志
     * 用于"目标详情"页面展示相关日志
     *
     * @param userId 当前登录用户 ID
     * @param goalId 目标 ID
     * @return 日志列表（按 log_date DESC, id DESC 排序）
     */
    List<GrowthLog> listByGoal(Long userId, Long goalId);
}
