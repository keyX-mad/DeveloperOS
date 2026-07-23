package com.keyx.module.growth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.common.BaseController;
import com.keyx.common.R;
import com.keyx.module.chat.dto.response.PageVO;
import com.keyx.module.growth.dto.request.CreateTaskRequest;
import com.keyx.module.growth.dto.request.UpdateTaskRequest;
import com.keyx.module.growth.dto.request.UpdateTaskStatusRequest;
import com.keyx.module.growth.dto.response.TaskVO;
import com.keyx.module.growth.entity.LearningTask;
import com.keyx.module.growth.enums.TaskStatus;
import com.keyx.module.growth.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
 * 学习任务 Controller
 *
 * 暴露 5 个 REST 接口：
 * - POST   /api/growth/goals/{goalId}/tasks       在目标下创建任务
 * - GET    /api/growth/goals/{goalId}/tasks       查询目标下的任务（分页）
 * - PUT    /api/growth/tasks/{id}                 修改任务
 * - PATCH  /api/growth/tasks/{id}/status          修改任务状态（⭐ 触发进度重算）
 * - DELETE /api/growth/tasks/{id}                 删除任务
 */
@RestController
@RequestMapping("/api/growth")
public class TaskController extends BaseController {

    @Autowired
    private TaskService taskService;

    // ============================================
    // ① 在目标下创建任务
    // ============================================
    @PostMapping("/goals/{goalId}/tasks")
    public R<TaskVO> create(@PathVariable Long goalId,
                            @Valid @RequestBody CreateTaskRequest req) {
        Long userId = currentUserId();
        LearningTask task = taskService.create(userId, goalId, req);
        return R.ok(toTaskVO(task));
    }

    // ============================================
    // ② 查询目标下的任务列表（分页）
    // ============================================
    @GetMapping("/goals/{goalId}/tasks")
    public R<PageVO<TaskVO>> list(
            @PathVariable Long goalId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUserId();
        Page<LearningTask> page = taskService.listByGoal(userId, goalId, status, current, size);
        return R.ok(toTaskPageVO(page));
    }

    // ============================================
    // ③ 修改任务
    // ============================================
    @PutMapping("/tasks/{id}")
    public R<TaskVO> update(@PathVariable Long id,
                            @Valid @RequestBody UpdateTaskRequest req) {
        Long userId = currentUserId();
        LearningTask task = taskService.update(userId, id, req);
        return R.ok(toTaskVO(task));
    }

    // ============================================
    // ④ 修改任务状态（⭐ 核心）
    // ============================================
    @PatchMapping("/tasks/{id}/status")
    public R<TaskVO> updateStatus(@PathVariable Long id,
                                  @Valid @RequestBody UpdateTaskStatusRequest req) {
        Long userId = currentUserId();
        LearningTask task = taskService.updateStatus(userId, id, req.getStatus());
        return R.ok(toTaskVO(task));
    }

    // ============================================
    // ⑤ 删除任务
    // ============================================
    @DeleteMapping("/tasks/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long userId = currentUserId();
        taskService.delete(userId, id);
        return R.ok();
    }

    // ============================================
    // 转换方法（Entity → VO）
    // ============================================

    private TaskVO toTaskVO(LearningTask t) {
        if (t == null) return null;
        TaskVO vo = new TaskVO();
        vo.setId(t.getId());
        vo.setGoalId(t.getGoalId());
        vo.setTitle(t.getTitle());
        vo.setDescription(t.getDescription());
        vo.setStatus(t.getStatus());
        vo.setStatusLabel(getTaskStatusLabel(t.getStatus()));
        vo.setPriority(t.getPriority());
        vo.setScheduledDate(t.getScheduledDate());
        vo.setDueAt((t.getDueAt()));
        vo.setSortOrder(t.getSortOrder());
        vo.setCompletedAt((t.getCompletedAt()));
        vo.setCreatedAt((t.getCreatedAt()));
        vo.setUpdatedAt((t.getUpdatedAt()));
        return vo;
    }

    private PageVO<TaskVO> toTaskPageVO(Page<LearningTask> page) {
        List<TaskVO> vos = page.getRecords().stream()
                .map(this::toTaskVO)
                .collect(Collectors.toList());

        PageVO<TaskVO> result = new PageVO<>();
        result.setRecords(vos);
        result.setTotal(page.getTotal());
        result.setCurrent((int) page.getCurrent());
        result.setSize((int) page.getSize());
        return result;
    }

    private String getTaskStatusLabel(String code) {
        if (code == null) return null;
        try {
            return TaskStatus.fromCode(code).getDescription();
        } catch (Exception e) {
            return code;
        }
    }

    /**
     * Instant → LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
