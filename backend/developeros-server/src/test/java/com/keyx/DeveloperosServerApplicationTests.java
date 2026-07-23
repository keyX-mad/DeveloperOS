package com.keyx;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.module.chat.entity.Conversation;
import com.keyx.module.chat.entity.Message;
import com.keyx.module.chat.enums.MessageStatus;
import com.keyx.module.chat.mapper.ConversationMapper;
import com.keyx.module.chat.mapper.MessageMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DeveloperosServerApplicationTests {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void mybatisPlusSelectPageDoesNotTriggerGetSqlFirst() {
        conversationMapper.selectPage(
                new Page<>(1, 1),
                Wrappers.lambdaQuery(Conversation.class)
                        .eq(Conversation::getUserId, -1L)
                        .orderByDesc(Conversation::getUpdatedAt)
        );

        messageMapper.selectPage(
                new Page<>(1, 1),
                Wrappers.lambdaQuery(Message.class)
                        .eq(Message::getConversationId, -1L)
                        .eq(Message::getStatus, MessageStatus.COMPLETED)
                        .orderByAsc(Message::getCreatedAt)
        );
    }
}
