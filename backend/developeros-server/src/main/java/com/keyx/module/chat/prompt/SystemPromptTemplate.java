package com.keyx.module.chat.prompt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 系统提示词配置
 *
 * 从 application.yml 读取：
 *   developer:
 *     ai:
 *       system-prompt: "..."
 *       history-size: 20
 *
 * @ConfigurationProperties 自动绑定配置到字段
 */
@Data
@Component
@ConfigurationProperties(prefix = "developer.ai")
public class SystemPromptTemplate {

    /**
     * 系统提示词（定义 AI 角色和行为）
     */
    private String systemPrompt;

    /**
     * Prompt 携带的历史消息数（默认 20）
     */
    private Integer historySize = 20;
}