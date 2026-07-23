package com.keyx.module.growth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 学习任务实体
 *
 * 对应表 learning_task
 */
@Data
@TableName("learning_task")
public class LearningTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goalId;
    private String title;
    private String description;
    private String status;       // TaskStatus.code
    private Integer priority;    // 1-5

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduledDate;

    private Instant dueAt;
    private Integer sortOrder;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
}