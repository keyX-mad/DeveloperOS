package com.keyx.module.growth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.common.BaseController;
import com.keyx.common.R;
import com.keyx.module.chat.dto.response.PageVO;
import com.keyx.module.growth.dto.request.CreateGoalRequest;
import com.keyx.module.growth.dto.request.UpdateGoalRequest;
import com.keyx.module.growth.dto.response.GoalVO;
import com.keyx.module.growth.entity.LearningGoal;
import com.keyx.module.growth.enums.GoalStatus;
import com.keyx.module.growth.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习目标 Controller
 *
 * 暴露 6 个 REST 接口：
 * - POST   /api/growth/goals               创建目标
 * - GET    /api/growth/goals               分页查询（?status=&current=&size=）
 * - GET    /api/growth/goals/{id}          查单个目标
 * - PUT    /api/growth/goals/{id}          修改目标
 * - DELETE /api/growth/goals/{id}          删除目标
 * - POST   /api/growth/goals/{id}/complete 标记完成
 */
@RestController
@RequestMapping("/api/growth")
public class GoalController extends BaseController {

    @Autowired
    private GoalService goalService;

    // ============================================
    // ① 创建目标
    // ============================================
    @PostMapping("/goals")
    public R<GoalVO> create(@Valid @RequestBody CreateGoalRequest req) {
        Long userId = currentUserId();
        LearningGoal goal = goalService.create(userId, req);
        return R.ok(toGoalVO(goal));
    }

    // ============================================
    // ② 分页查询目标列表
    // ============================================
    @GetMapping("/goals")
    public R<PageVO<GoalVO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserId();
        Page<LearningGoal> page = goalService.list(userId, status, current, size);
        return R.ok(toGoalPageVO(page));
    }

    // ============================================
    // ③ 查询单个目标
    // ============================================
    @GetMapping("/goals/{id}")
    public R<GoalVO> getById(@PathVariable Long id) {
        Long userId = currentUserId();
        LearningGoal goal = goalService.getById(userId, id);
        return R.ok(toGoalVO(goal));
    }

    // ============================================
    // ④ 修改目标
    // ============================================
    @PutMapping("/goals/{id}")
    public R<GoalVO> update(@PathVariable Long id,
                            @Valid @RequestBody UpdateGoalRequest req) {
        Long userId = currentUserId();
        LearningGoal goal = goalService.update(userId, id, req);
        return R.ok(toGoalVO(goal));
    }

    // ============================================
    // ⑤ 删除目标
    // ============================================
    @DeleteMapping("/goals/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long userId = currentUserId();
        goalService.delete(userId, id);
        return R.ok();
    }

    // ============================================
    // ⑥ 标记目标完成
    // ============================================
    @PostMapping("/goals/{id}/complete")
    public R<GoalVO> complete(@PathVariable Long id) {
        Long userId = currentUserId();
        LearningGoal goal = goalService.complete(userId, id);
        return R.ok(toGoalVO(goal));
    }

    // ============================================
    // 转换方法（Entity → VO）
    // ============================================

    private GoalVO toGoalVO(LearningGoal g) {
        if (g == null) return null;
        GoalVO vo = new GoalVO();
        vo.setId(g.getId());
        vo.setTitle(g.getTitle());
        vo.setDescription(g.getDescription());
        vo.setStatus(g.getStatus());
        vo.setStatusLabel(getGoalStatusLabel(g.getStatus()));
        vo.setPriority(g.getPriority());
        vo.setProgressPercent(g.getProgressPercent());
        vo.setStartDate(g.getStartDate());
        vo.setTargetDate(g.getTargetDate());
        vo.setCompletedAt(g.getCompletedAt());
        vo.setCreatedAt(g.getCreatedAt());
        vo.setUpdatedAt(g.getUpdatedAt());
        return vo;
    }

    private PageVO<GoalVO> toGoalPageVO(Page<LearningGoal> page) {
        List<GoalVO> vos = page.getRecords().stream()
                .map(this::toGoalVO)
                .collect(Collectors.toList());

        PageVO<GoalVO> result = new PageVO<>();
        result.setRecords(vos);
        result.setTotal(page.getTotal());
        result.setCurrent((int) page.getCurrent());
        result.setSize((int) page.getSize());
        return result;
    }

    private String getGoalStatusLabel(String code) {
        if (code == null) return null;
        try {
            return GoalStatus.fromCode(code).getDescription();
        } catch (Exception e) {
            return code;  // 兜底返回 code
        }
    }

    /**
     * Instant → LocalDateTime（数据库 → 前端）
     */
    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
