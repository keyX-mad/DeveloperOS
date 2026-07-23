package com.keyx.module.growth.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class GrowthLogVO {
    private Long id;
    private Long goalId;
    private String logType;
    private String logTypeLabel;
    private String title;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate logDate;

    private Integer durationMinutes;
    private Boolean isAiGenerated;
    private Instant createdAt;
    private Instant updatedAt;
}