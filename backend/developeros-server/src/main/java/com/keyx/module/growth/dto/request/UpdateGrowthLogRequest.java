package com.keyx.module.growth.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 修改成长日志请求 DTO（partial update）
 *
 * 前端发 PUT /api/growth/logs/{id} 时 JSON 长这样（所有字段都可选）：
 * {
 *   "title": "新标题",
 *   "content": "新内容",
 *   "logType": "reflection",
 *   "logDate": "2026-07-24",
 *   "durationMinutes": 90
 *   // goalId 不允许修改（避免日志脱离原目标上下文）
 * }
 *
 * 注意：goalId 不允许修改，避免破坏"日志-目标"的归属关系
 *      如要更换关联，请删除后重建
 */
@Data
public class UpdateGrowthLogRequest {

    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    private String content;

    /**
     * 日志类型：daily / weekly / milestone / reflection / other
     */
    private String logType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate logDate;

    @Min(value = 0, message = "学习时长不能为负")
    @Max(value = 24 * 60, message = "单日学习时长不能超过 1440 分钟")
    private Integer durationMinutes;
}
