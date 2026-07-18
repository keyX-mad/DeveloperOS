package com.keyx.module.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.module.chat.dto.request.CreateConversationRequest;
import com.keyx.module.chat.dto.request.UpdateConversationRequest;
import com.keyx.module.chat.entity.Conversation;

public interface ConversationService {

    Conversation create(Long userId, CreateConversationRequest req);

    Page<Conversation> list(Long userId, int current, int size);

    Conversation getById(Long userId, Long conversationId);

    Conversation update(Long userId, Long conversationId, UpdateConversationRequest req);

    void delete(Long userId, Long conversationId);

}
