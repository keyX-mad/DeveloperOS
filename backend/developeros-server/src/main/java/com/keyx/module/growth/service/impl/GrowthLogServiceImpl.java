package com.keyx.module.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keyx.module.growth.dto.request.CreateGrowthLogRequest;
import com.keyx.module.growth.dto.request.UpdateGrowthLogRequest;
import com.keyx.module.growth.entity.GrowthLog;
import com.keyx.module.growth.enums.LogType;
import com.keyx.module.growth.exception.GrowthException;
import com.keyx.module.growth.mapper.GrowthLogMapper;
import com.keyx.module.growth.service.GoalService;
import com.keyx.module.growth.service.GrowthLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 成长日志 Service 实现
 *
 * 核心安全要求：所有 public 方法都必须先校验 userId 归属！
 *
 * 关键设计：
 * - create 时如果有关联 goal，必须校验 goal 属于当前用户
 *   （DB 复合外键 fk_growth_log_goal_user 兜底，Service 主动校验给友好错误）
 * - goalId 一旦创建不允许修改（update DTO 不含 goalId 字段）
 * - 删除目标时关联日志的 goal_id 被 DB 置 NULL（不影响日志本身）
 *
 * 列表查询统一用 Wrappers.lambdaQuery(GrowthLog.class) 绕开 OGNL
 */
@Service
public class GrowthLogServiceImpl extends ServiceImpl<GrowthLogMapper, GrowthLog> implements GrowthLogService {

    @Autowired
    private GrowthLogMapper growthLogMapper;

    /**
     * 注入 GoalService 用于校验 goal 归属
     * 单向依赖：GrowthLogService → GoalService
     */
    @Autowired
    private GoalService goalService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GrowthLog create(Long userId, CreateGrowthLogRequest req) {
        // 1. 如果有关联 goal，校验 goal 属于当前用户
        if (req.getGoalId() != null) {
            // DB 复合外键会兜底（goal_id 不存在或不属于 userId 会报错）
            // Service 主动校验是为了给友好错误信息（goalNotFound）
            goalService.getById(userId, req.getGoalId());
        }

        // 2. 校验日志类型合法性
        LogType logType = LogType.fromCode(req.getLogType());

        // 3. 构建实体
        Instant now = Instant.now();
        GrowthLog log = new GrowthLog();
        log.setUserId(userId);
        log.setGoalId(req.getGoalId());  // 可空
        log.setLogType(logType.getCode());
        log.setTitle(req.getTitle().trim());
        log.setContent(req.getContent());
        log.setLogDate(req.getLogDate() != null ? req.getLogDate() : LocalDate.now());
        log.setDurationMinutes(req.getDurationMinutes());
        log.setIsAiGenerated(req.getIsAiGenerated() != null ? req.getIsAiGenerated() : false);

        // 手动 set 时间
        log.setCreatedAt(now);
        log.setUpdatedAt(now);

        // 4. 持久化
        growthLogMapper.insert(log);
        return log;
    }

    @Override
    public Page<GrowthLog> list(Long userId, String logType, int current, int size) {
        Page<GrowthLog> page = new Page<>(current, size);

        LambdaQueryWrapper<GrowthLog> wrapper = Wrappers.lambdaQuery(GrowthLog.class)
                .eq(GrowthLog::getUserId, userId)
                .orderByDesc(GrowthLog::getLogDate)  // 最新日志在前
                .orderByDesc(GrowthLog::getId);      // 同日按 id 倒序

        // 可选类型过滤
        if (logType != null && !logType.isBlank()) {
            LogType.fromCode(logType);  // 顺便校验合法性
            wrapper.eq(GrowthLog::getLogType, logType);
        }

        return growthLogMapper.selectPage(page, wrapper);
    }

    /**
     * 查询日志详情（带 userId 校验）
     */
    @Override
    public GrowthLog getById(Long userId, Long logId) {
        GrowthLog log = growthLogMapper.selectById(logId);

        if (log == null || !log.getUserId().equals(userId)) {
            throw GrowthException.logNotFound();
        }
        return log;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GrowthLog update(Long userId, Long logId, UpdateGrowthLogRequest req) {
        // 1. 校验归属
        GrowthLog log = getById(userId, logId);

        // 2. partial update（不允许改 goalId）
        if (req.getTitle() != null) {
            log.setTitle(req.getTitle().trim());
        }
        if (req.getContent() != null) {
            log.setContent(req.getContent());
        }
        if (req.getLogType() != null) {
            LogType.fromCode(req.getLogType());  // 校验合法性
            log.setLogType(req.getLogType());
        }
        if (req.getLogDate() != null) {
            log.setLogDate(req.getLogDate());
        }
        if (req.getDurationMinutes() != null) {
            log.setDurationMinutes(req.getDurationMinutes());
        }

        // 3. 持久化
        growthLogMapper.updateById(log);
        return log;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long logId) {
        // 1. 校验归属
        getById(userId, logId);

        // 2. 删除
        growthLogMapper.deleteById(logId);
    }

    /**
     * 查询目标关联的所有日志
     * 用于"目标详情"页面
     */
    @Override
    public List<GrowthLog> listByGoal(Long userId, Long goalId) {
        // 1. 校验 goal 归属当前用户
        goalService.getById(userId, goalId);

        // 2. 查该目标关联的所有日志（包括 goalId = goalId 的）
        return growthLogMapper.selectList(
                Wrappers.lambdaQuery(GrowthLog.class)
                        .eq(GrowthLog::getUserId, userId)
                        .eq(GrowthLog::getGoalId, goalId)
                        .orderByDesc(GrowthLog::getLogDate)
                        .orderByDesc(GrowthLog::getId));
    }
}
