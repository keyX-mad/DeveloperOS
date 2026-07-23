package com.keyx.module.growth.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 写成长日志请求 DTO
 *
 * 前端发 POST /api/growth/logs 时 JSON 长这样：
 * {
 *   "title": "完成 Spring Security 登录模块",
 *   "content": "今天实现了 JWT 登录过滤器，跑了 13 个单元测试都通过了",
 *   "logType": "daily",
 *   "logDate": "2026-07-23",
 *   "durationMinutes": 120,
 *   "goalId": 42
 * }
 *
 * goalId 可选：日志可以不属于任何目标
 */
@Data
public class CreateGrowthLogRequest {

    @NotBlank(message = "日志标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    @NotBlank(message = "日志内容不能为空")
    private String content;

    /**
     * 日志类型：daily / weekly / milestone / reflection / other
     * 必填（前端显式选择，避免歧义）
     */
    @NotBlank(message = "日志类型不能为空")
    private String logType;

    /**
     * 业务日期（默认今天）
     * 可选：null 时 Service 用 LocalDate.now()
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate logDate;

    @Min(value = 0, message = "学习时长不能为负")
    @Max(value = 24 * 60, message = "单日学习时长不能超过 1440 分钟")
    private Integer durationMinutes;

    /**
     * 可选：关联的目标 ID
     * Service 校验 goal 必须属于当前用户（复合外键保护）
     */
    private Long goalId;

    /**
     * 是否由 AI 生成（V1 暂不自动生成，留口子）
     * 可选，默认 false
     */
    private Boolean isAiGenerated;
}
