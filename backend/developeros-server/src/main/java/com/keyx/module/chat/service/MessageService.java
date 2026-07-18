package com.keyx.module.chat.service;

import com.keyx.module.chat.entity.Message;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface MessageService {
    //保存用户信息
    Message saveUserMessage(Long userId, Long conversationId, String content);
    //AI开始生成
    Message saveAssistantStart(Long userId, Long conversationId);

    Message completeAssistant(Long userId, Long messageId, String content, Integer tokenCount);

    Message failAssistant(Long userId, Long messageId, String errorMessage);

    Message stopAssistant(Long userId, Long messageId, String partialContent);

    Page<Message> listByConversation(Long userId, Long conversationId, int current, int size);

}
