package com.keyx.module.growth.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class TaskVO {
    private Long id;
    private Long goalId;
    private String title;
    private String description;
    private String status;
    private String statusLabel;
    private Integer priority;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduledDate;

    private Instant dueAt;
    private Integer sortOrder;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
}