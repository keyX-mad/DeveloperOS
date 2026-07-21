package com.keyx.module.chat.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 通用分页响应 VO
 *
 * 后端返回给前端的分页数据格式：
 * {
 *   "records": [...],   // 当前页数据（任意类型）
 *   "total": 100,        // 总记录数
 *   "current": 1,        // 当前页（从 1 开始）
 *   "size": 10           // 每页大小
 * }
 *
 * 用法示例：
 *   PageVO<MessageVO>    // 消息分页
 *   PageVO<ConversationVO> // 会话分页
 *   PageVO<UserVO>        // 用户分页
 *
 * 泛型 <T> 让一个类可以包装任何类型的数据列表
 */
@Data
public class PageVO<T> {

    /**
     * 当前页数据列表
     * T 是泛型：消息列表、会话列表、用户列表都能用
     */
    private List<T> records;

    /**
     * 总记录数
     * 前端用来算总页数：totalPages = (total + size - 1) / size
     */
    private Long total;

    /**
     * 当前页码
     * 从 1 开始（第 1 页、第 2 页...）
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;
}
