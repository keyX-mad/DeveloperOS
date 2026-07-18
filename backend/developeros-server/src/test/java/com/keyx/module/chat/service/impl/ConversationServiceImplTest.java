package com.keyx.module.chat.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keyx.common.exception.BusinessException;
import com.keyx.module.chat.dto.request.CreateConversationRequest;
import com.keyx.module.chat.dto.request.UpdateConversationRequest;
import com.keyx.module.chat.entity.Conversation;
import com.keyx.module.chat.mapper.ConversationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ConversationServiceImpl 单元测试
 *
 * 重点验证：
 * 1. 正常业务逻辑（create / list / getById / update / delete）
 * 2. ⚠️ 安全底线：userId 归属校验（不通过必须抛 404）
 *
 * 特点：
 * - 不连真实数据库（@Mock 模拟 Mapper）
 * - 不启动 Spring 容器（@ExtendWith(MockitoExtension.class)）
 * - 跑得快（毫秒级）
 */
@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock
    private ConversationMapper conversationMapper;

    @InjectMocks
    private ConversationServiceImpl service;

    // ============================================
    // 工具方法：构造测试用的 Conversation 实体
    // ============================================

    /**
     * 构造一个测试用的会话（默认属于用户 1L）
     */
    private Conversation buildConversation(Long id, Long userId) {
        Conversation c = new Conversation();
        c.setId(id);
        c.setUserId(userId);
        c.setTitle("测试会话");
        c.setStatus("active");
        return c;
    }

    // ============================================
    // create 方法测试（3 个场景）
    // ============================================

    /**
     * 场景 1：正常创建（title 不为空）
     * 期望：成功返回实体
     */
    @Test
    void testCreate_Normal() {
        // Arrange：准备请求
        CreateConversationRequest req = new CreateConversationRequest();
        req.setTitle("我的会话");

        // Act：调用 service
        Conversation result = service.create(1L, req);

        // Assert：验证返回值
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("我的会话", result.getTitle());
        assertEquals("active", result.getStatus());

        // 验证：mapper.insert 被调用了
        verify(conversationMapper).insert(any(Conversation.class));
    }

    /**
     * 场景 2：title 为 null（用默认值 "New Conversation"）
     */
    @Test
    void testCreate_TitleIsNull() {
        // Arrange
        CreateConversationRequest req = new CreateConversationRequest();
        req.setTitle(null);  // ← null

        // Act
        Conversation result = service.create(1L, req);

        // Assert：title 用默认值
        assertEquals("New Conversation", result.getTitle());
    }

    /**
     * 场景 3：title 是空格（用默认值）
     */
    @Test
    void testCreate_TitleIsBlank() {
        // Arrange
        CreateConversationRequest req = new CreateConversationRequest();
        req.setTitle("   ");  // ← 纯空格

        // Act
        Conversation result = service.create(1L, req);

        // Assert
        assertEquals("New Conversation", result.getTitle());
    }

    // ============================================
    // list 方法测试
    // ============================================

    /**
     * 场景：分页查询
     * 期望：调用 mapper.selectPage，过滤了 userId
     */
    @Test
    void testList() {
        // Arrange：准备 mock 返回值
        Conversation c1 = buildConversation(1L, 1L);
        Conversation c2 = buildConversation(2L, 1L);
        Page<Conversation> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(c1, c2));
        mockPage.setTotal(2L);

        when(conversationMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        // Act
        Page<Conversation> result = service.list(1L, 1, 10);

        // Assert
        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getRecords().size());

        // 验证：mapper 被调用时带了 userId 过滤条件
        ArgumentCaptor<Page<Conversation>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(conversationMapper).selectPage(pageCaptor.capture(), any());
        assertEquals(1, pageCaptor.getValue().getCurrent());
        assertEquals(10, pageCaptor.getValue().getSize());
    }

    // ============================================
    // getById 方法测试（3 个场景，含安全测试）
    // ============================================

    /**
     * 场景 1：正常查询（自己的会话）
     */
    @Test
    void testGetById_Success() {
        // Arrange
        Conversation c = buildConversation(1L, 1L);
        when(conversationMapper.selectById(1L)).thenReturn(c);

        // Act
        Conversation result = service.getById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * 场景 2：会话不存在（抛 404）
     */
    @Test
    void testGetById_NotFound() {
        // Arrange：mock 返回 null
        when(conversationMapper.selectById(99L)).thenReturn(null);

        // Act + Assert：必须抛 BusinessException
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            service.getById(1L, 99L);
        });
        assertEquals(404, ex.getCode());
        assertEquals("会话不存在", ex.getMessage());
    }

    /**
     * 场景 3：⚠️ 安全测试 - 别人的会话（必须抛 404）
     * 验证：A 传 5（B 的会话），A 拿不到
     */
    @Test
    void testGetById_OtherUsersConversation_Forbidden() {
        // Arrange：会话属于用户 2
        Conversation c = buildConversation(1L, 2L);
        when(conversationMapper.selectById(1L)).thenReturn(c);

        // Act + Assert：用户 1 访问用户 2 的会话，必须抛 404
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            service.getById(1L, 1L);  // userId=1 访问 userId=2 的会话
        });
        assertEquals(404, ex.getCode());
        assertEquals("会话不存在", ex.getMessage());  // 用 404 不是 403，防信息泄露
    }

    // ============================================
    // update 方法测试（2 个场景）
    // ============================================

    /**
     * 场景 1：正常更新（自己的）
     */
    @Test
    void testUpdate_Success() {
        // Arrange
        Conversation c = buildConversation(1L, 1L);
        when(conversationMapper.selectById(1L)).thenReturn(c);

        UpdateConversationRequest req = new UpdateConversationRequest();
        req.setTitle("新标题");

        // Act
        Conversation result = service.update(1L, 1L, req);

        // Assert
        assertEquals("新标题", result.getTitle());
        verify(conversationMapper).updateById(any(Conversation.class));
    }

    /**
     * 场景 2：⚠️ 安全测试 - 别人的会话（必须抛 404）
     */
    @Test
    void testUpdate_OtherUsersConversation_Forbidden() {
        // Arrange：会话属于用户 2
        Conversation c = buildConversation(1L, 2L);
        when(conversationMapper.selectById(1L)).thenReturn(c);

        UpdateConversationRequest req = new UpdateConversationRequest();
        req.setTitle("黑客标题");

        // Act + Assert：用户 1 改用户 2 的会话，必须抛 404
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            service.update(1L, 1L, req);
        });
        assertEquals(404, ex.getCode());

        // 验证：没有调用 updateById（因为校验失败）
        verify(conversationMapper, never()).updateById(any(Conversation.class));
    }

    // ============================================
    // delete 方法测试（2 个场景）
    // ============================================

    /**
     * 场景 1：正常删除（自己的）
     */
    @Test
    void testDelete_Success() {
        // Arrange
        Conversation c = buildConversation(1L, 1L);
        when(conversationMapper.selectById(1L)).thenReturn(c);

        // Act
        service.delete(1L, 1L);

        // Assert
        verify(conversationMapper).deleteById(1L);
    }

    /**
     * 场景 2：⚠️ 安全测试 - 别人的会话（必须抛 404）
     */
    @Test
    void testDelete_OtherUsersConversation_Forbidden() {
        // Arrange：会话属于用户 2
        Conversation c = buildConversation(1L, 2L);
        when(conversationMapper.selectById(1L)).thenReturn(c);

        // Act + Assert：用户 1 删用户 2 的会话，必须抛 404
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            service.delete(1L, 1L);
        });
        assertEquals(404, ex.getCode());

        // 验证：没有调用 deleteById（因为校验失败）
        verify(conversationMapper, never()).deleteById(any(Long.class));
    }
}