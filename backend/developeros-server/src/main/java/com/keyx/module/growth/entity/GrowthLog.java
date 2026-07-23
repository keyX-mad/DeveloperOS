package com.keyx.module.growth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 成长日志实体
 *
 * 对应表 growth_log
 *
 * 注意：metadata 是 JSONB，当前用 String 占位，
 * updateStrategy = NEVER 让 updateById 跳过该字段（与 User.preferences 一致）
 */
@Data
@TableName("growth_log")
public class GrowthLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long goalId;          // 可空
    private String logType;       // LogType.code
    private String title;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate logDate;

    private Integer durationMinutes;
    private Boolean isAiGenerated;

    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String metadata;      // JSONB 占位

    private Instant createdAt;
    private Instant updatedAt;
}
