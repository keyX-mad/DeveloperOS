package com.keyx.module.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.common.BaseController;
import com.keyx.common.R;
import com.keyx.module.chat.dto.request.CreateConversationRequest;
import com.keyx.module.chat.dto.request.UpdateConversationRequest;
import com.keyx.module.chat.dto.response.ConversationVO;
import com.keyx.module.chat.dto.response.MessageVO;
import com.keyx.module.chat.dto.response.PageVO;
import com.keyx.module.chat.entity.Conversation;
import com.keyx.module.chat.entity.Message;
import com.keyx.module.chat.service.ConversationService;
import com.keyx.module.chat.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话 Controller
 *
 * 暴露 6 个 REST 接口：
 * - POST   /api/chat/conversations               创建会话
 * - GET    /api/chat/conversations               分页查询
 * - GET    /api/chat/conversations/{id}          查单个
 * - PUT    /api/chat/conversations/{id}          改标题
 * - DELETE /api/chat/conversations/{id}          删除
 * - GET    /api/chat/conversations/{id}/messages  查消息
 */
@RestController
@RequestMapping("/api/chat")
public class ConversationController extends BaseController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    // ============================================
    // ① 创建会话
    // ============================================
    @PostMapping("/conversations")
    public R<ConversationVO> create(@Valid @RequestBody CreateConversationRequest req) {
        Long userId = currentUserId();
        Conversation c = conversationService.create(userId, req);
        return R.ok(toConversationVO(c));
    }

    // ============================================
    // ② 分页查询
    // ============================================
    @GetMapping("/conversations")
    public R<PageVO<ConversationVO>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserId();
        Page<Conversation> page = conversationService.list(userId, current, size);
        return R.ok(toConversationPageVO(page));
    }

    // ============================================
    // ③ 查单个会话
    // ============================================
    @GetMapping("/conversations/{id}")
    public R<ConversationVO> getById(@PathVariable Long id) {
        Long userId = currentUserId();
        Conversation c = conversationService.getById(userId, id);
        return R.ok(toConversationVO(c));
    }

    // ============================================
    // ④ 修改会话标题
    // ============================================
    @PutMapping("/conversations/{id}")
    public R<ConversationVO> update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateConversationRequest req) {
        Long userId = currentUserId();
        Conversation c = conversationService.update(userId, id, req);
        return R.ok(toConversationVO(c));
    }

    // ============================================
    // ⑤ 删除会话
    // ============================================
    @DeleteMapping("/conversations/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long userId = currentUserId();
        conversationService.delete(userId, id);
        return R.ok();
    }

    // ============================================
    // ⑥ 查会话的消息列表
    // ============================================
    @GetMapping("/conversations/{id}/messages")
    public R<PageVO<MessageVO>> listMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUserId();
        Page<Message> page = messageService.listByConversation(userId, id, current, size);
        return R.ok(toMessagePageVO(page));
    }

    // ============================================
    // 转换工具方法
    // ============================================

    private ConversationVO toConversationVO(Conversation c) {
        ConversationVO vo = new ConversationVO();
        vo.setId(c.getId());
        vo.setTitle(c.getTitle());
        vo.setModelName(c.getModelName());
        vo.setStatus(c.getStatus());
        vo.setLastMessageAt(toLocalDateTime(c.getLastMessageAt()));
        vo.setCreatedAt(toLocalDateTime(c.getCreatedAt()));
        return vo;
    }

    private PageVO<ConversationVO> toConversationPageVO(Page<Conversation> page) {
        List<ConversationVO> vos = page.getRecords().stream()
                .map(this::toConversationVO)
                .collect(Collectors.toList());

        PageVO<ConversationVO> result = new PageVO<>();
        result.setRecords(vos);
        result.setTotal(page.getTotal());
        result.setCurrent((int) page.getCurrent());
        result.setSize((int) page.getSize());
        return result;
    }

    private PageVO<MessageVO> toMessagePageVO(Page<Message> page) {
        List<MessageVO> vos = page.getRecords().stream()
                .map(this::toMessageVO)
                .collect(Collectors.toList());

        PageVO<MessageVO> result = new PageVO<>();
        result.setRecords(vos);
        result.setTotal(page.getTotal());
        result.setCurrent((int) page.getCurrent());
        result.setSize((int) page.getSize());
        return result;
    }

    private MessageVO toMessageVO(Message m) {
        MessageVO vo = new MessageVO();
        vo.setId(m.getId());
        vo.setRole(m.getRole());
        vo.setContent(m.getContent());
        vo.setStatus(m.getStatus());
        vo.setModelName(m.getModelName());
        vo.setTokenCount(m.getTokenCount());
        vo.setCreatedAt(toLocalDateTime(m.getCreatedAt()));
        return vo;
    }

    /**
     * Instant → LocalDateTime（数据库 → 前端）
     */
    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
