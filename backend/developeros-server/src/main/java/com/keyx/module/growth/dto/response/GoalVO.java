package com.keyx.module.growth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 学习目标 VO（返回给前端）
 *
 * 不直接暴露 Entity，按字段映射防止泄漏内部字段
 */
@Data
public class GoalVO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String statusLabel;   // 中文描述
    private Integer priority;
    private Integer progressPercent;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;

    // 统计字段（详情时填充）
    private Integer taskCount;
    private Integer completedTaskCount;
}