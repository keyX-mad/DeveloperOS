package com.keyx.module.chat.prompt;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.module.chat.entity.Message;
import com.keyx.module.chat.enums.MessageRole;
import com.keyx.module.chat.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Prompt 构建器
 *
 * 负责把"数据库消息" + "系统提示词" + "用户当前消息"组装成 Spring AI 的 Prompt 对象
 *
 * Prompt 结构：
 *   [SystemMessage]   system-prompt
 *   [UserMessage]     历史消息 1（最早的）
 *   [AssistantMessage] 历史消息 2
 *   ...
 *   [UserMessage]     当前 user 消息
 *
 * V2/V3 预留：injectMemory() 注入长期记忆，injectRagContext() 注入 RAG 上下文
 */
@Component
@RequiredArgsConstructor
public class PromptBuilder {

    private final SystemPromptTemplate systemPromptTemplate;
    private final MessageMapper messageMapper;

    /**
     * 构建完整的 Prompt
     *
     * @param conversationId 会话 ID
     * @param userMessage   当前用户消息
     * @return Spring AI 的 Prompt 对象
     */
    public Prompt build(Long conversationId, String userMessage) {
        // 用 List<org.springframework.ai.chat.messages.Message> 而不是 List<Message>
        // 避免和实体的 Message 冲突
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();

        // 1. System 消息（AI 的人设）
        messages.add(new SystemMessage(systemPromptTemplate.getSystemPrompt()));

        // 2. 预留：注入长期记忆（V2 阶段实现）
        //    injectMemory(messages, conversationId);

        // 3. 预留：注入 RAG 上下文（V3 阶段实现）
        //    injectRagContext(messages, userMessage);

        // 4. 历史消息（最近 N 条，按时间正序）
        List<Message> history = loadHistory(conversationId);
        for (Message msg : history) {
            messages.add(toAiMessage(msg));
        }

        // 5. 当前 user 消息
        messages.add(new UserMessage(userMessage));

        return new Prompt(messages);
    }

    /**
     * 从数据库加载最近 N 条历史消息
     * 按时间正序（聊天从早到晚）
     */
    private List<Message> loadHistory(Long conversationId) {
        int size = systemPromptTemplate.getHistorySize() != null
                ? systemPromptTemplate.getHistorySize()
                : 20;

        Page<Message> page = new Page<>(1, size);
        Page<Message> result = messageMapper.selectPage(page,
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByAsc(Message::getCreatedAt)
        );

        return result.getRecords();
    }

    /**
     * 把数据库 Message 转换成 Spring AI 的 Message
     */
    private org.springframework.ai.chat.messages.Message toAiMessage(Message msg) {
        MessageRole role = msg.getRole();
        if (role == MessageRole.USER) {
            return new UserMessage(msg.getContent());
        } else if (role == MessageRole.ASSISTANT) {
            return new AssistantMessage(msg.getContent());
        } else {
            // SYSTEM / TOOL（V1 简单处理，统一用 SystemMessage）
            return new SystemMessage(msg.getContent());
        }
    }

    // ============================================
    // V2/V3 预留方法
    // ============================================

    /**
     * 注入长期记忆（V2 阶段实现）
     * 从 memory 表查用户的偏好、目标等
     */
    private void injectMemory(List<org.springframework.ai.chat.messages.Message> messages, Long conversationId) {
        // TODO V2: 从 memory 表查这个用户的长期记忆，拼到 messages 里
    }

    /**
     * 注入 RAG 上下文（V3 阶段实现）
     * 从知识库查相关内容
     */
    private void injectRagContext(List<org.springframework.ai.chat.messages.Message> messages, String userMessage) {
        // TODO V3: 用 userMessage 查知识库，拼到 messages 里
    }
}