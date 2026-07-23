package com.keyx.module.growth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 学习目标实体
 *
 * 对应表 learning_goal
 */
@Data
@TableName("learning_goal")
public class LearningGoal {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String description;
    private String status;       // GoalStatus.code
    private Integer priority;    // 1-5
    private Integer progressPercent;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
}