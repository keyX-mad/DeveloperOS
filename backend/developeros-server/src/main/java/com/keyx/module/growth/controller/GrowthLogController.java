package com.keyx.module.growth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.common.BaseController;
import com.keyx.common.R;
import com.keyx.module.chat.dto.response.PageVO;
import com.keyx.module.growth.dto.request.CreateGrowthLogRequest;
import com.keyx.module.growth.dto.request.UpdateGrowthLogRequest;
import com.keyx.module.growth.dto.response.GrowthLogVO;
import com.keyx.module.growth.entity.GrowthLog;
import com.keyx.module.growth.enums.LogType;
import com.keyx.module.growth.service.GrowthLogService;
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
 * 成长日志 Controller
 *
 * 暴露 5 个 REST 接口：
 * - POST   /api/growth/logs         写日志
 * - GET    /api/growth/logs         分页查询日志列表（?logType=&current=&size=）
 * - GET    /api/growth/logs/{id}    查单个日志
 * - PUT    /api/growth/logs/{id}    修改日志
 * - DELETE /api/growth/logs/{id}    删除日志
 */
@RestController
@RequestMapping("/api/growth")
public class GrowthLogController extends BaseController {

    @Autowired
    private GrowthLogService growthLogService;

    // ============================================
    // ① 写日志
    // ============================================
    @PostMapping("/logs")
    public R<GrowthLogVO> create(@Valid @RequestBody CreateGrowthLogRequest req) {
        Long userId = currentUserId();
        GrowthLog log = growthLogService.create(userId, req);
        return R.ok(toGrowthLogVO(log));
    }

    // ============================================
    // ② 分页查询日志列表
    // ============================================
    @GetMapping("/logs")
    public R<PageVO<GrowthLogVO>> list(
            @RequestParam(required = false) String logType,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserId();
        Page<GrowthLog> page = growthLogService.list(userId, logType, current, size);
        return R.ok(toGrowthLogPageVO(page));
    }

    // ============================================
    // ③ 查询单个日志
    // ============================================
    @GetMapping("/logs/{id}")
    public R<GrowthLogVO> getById(@PathVariable Long id) {
        Long userId = currentUserId();
        GrowthLog log = growthLogService.getById(userId, id);
        return R.ok(toGrowthLogVO(log));
    }

    // ============================================
    // ④ 修改日志
    // ============================================
    @PutMapping("/logs/{id}")
    public R<GrowthLogVO> update(@PathVariable Long id,
                                 @Valid @RequestBody UpdateGrowthLogRequest req) {
        Long userId = currentUserId();
        GrowthLog log = growthLogService.update(userId, id, req);
        return R.ok(toGrowthLogVO(log));
    }

    // ============================================
    // ⑤ 删除日志
    // ============================================
    @DeleteMapping("/logs/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long userId = currentUserId();
        growthLogService.delete(userId, id);
        return R.ok();
    }

    // ============================================
    // 转换方法（Entity → VO）
    // ============================================

    private GrowthLogVO toGrowthLogVO(GrowthLog l) {
        if (l == null) return null;
        GrowthLogVO vo = new GrowthLogVO();
        vo.setId(l.getId());
        vo.setGoalId(l.getGoalId());
        vo.setLogType(l.getLogType());
        vo.setLogTypeLabel(getLogTypeLabel(l.getLogType()));
        vo.setTitle(l.getTitle());
        vo.setContent(l.getContent());
        vo.setLogDate(l.getLogDate());
        vo.setDurationMinutes(l.getDurationMinutes());
        vo.setIsAiGenerated(l.getIsAiGenerated());
        vo.setCreatedAt(l.getCreatedAt());
        vo.setUpdatedAt(l.getUpdatedAt());
        return vo;
    }

    private PageVO<GrowthLogVO> toGrowthLogPageVO(Page<GrowthLog> page) {
        List<GrowthLogVO> vos = page.getRecords().stream()
                .map(this::toGrowthLogVO)
                .collect(Collectors.toList());

        PageVO<GrowthLogVO> result = new PageVO<>();
        result.setRecords(vos);
        result.setTotal(page.getTotal());
        result.setCurrent((int) page.getCurrent());
        result.setSize((int) page.getSize());
        return result;
    }

    private String getLogTypeLabel(String code) {
        if (code == null) return null;
        try {
            return LogType.fromCode(code).getDescription();
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

