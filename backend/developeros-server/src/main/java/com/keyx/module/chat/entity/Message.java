package com.keyx.module.chat.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keyx.module.chat.enums.MessageRole;
import com.keyx.module.chat.enums.MessageStatus;
import lombok.Data;

import java.time.Instant;

/**
 * 消息实体
 *
 * 对应数据库表：message
 * （每条用户/系统/AI/工具消息 = 一条 message）
 *
 * 数据库字段对照：
 *   id              BIGINT       PRIMARY KEY
 *   conversation_id BIGINT       所属会话（外键 → conversation.id）
 *   role            VARCHAR(20)  角色：system / user / assistant / tool（用枚举）
 *   content         TEXT         消息内容
 *   status          VARCHAR(20)  状态：streaming / completed / failed / stopped（用枚举）
 *   model_name      VARCHAR(100) 实际生成模型
 *   token_count     INTEGER      Token 数量
 *   error_message   TEXT         失败原因
 *   metadata        JSONB        元数据（工具调用、引用来源等）
 *   created_at      TIMESTAMPTZ  创建时间
 *   updated_at      TIMESTAMPTZ  最后修改时间
 */
@Data
@TableName("message")
public class Message {

    /**
     * 消息 ID（主键，数据库自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属会话 ID
     * 外键关联 conversation.id（数据库级联删除）
     */
    private Long conversationId;

    /**
     * 消息角色
     * ⭐ 用枚举 MessageRole（不是 String！）
     * MyBatis-Plus 通过 @EnumValue 自动存数据库的字符串（system/user/assistant/tool）
     */
    private MessageRole role;

    /**
     * 消息内容
     * 流式生成时可能暂时为空字符串
     */
    private String content;

    /**
     * 消息状态
     * ⭐ 用枚举 MessageStatus
     * streaming（流式中）/ completed（完成）/ failed（失败）/ stopped（中止）
     */
    private MessageStatus status;

    /**
     * 实际生成消息的模型名称
     * 可空（用户消息没有模型）
     */
    private String modelName;

    /**
     * Token 数量（用于统计费用 / 控制上下文长度）
     * 可空
     */
    private Integer tokenCount;

    /**
     * 生成失败时的错误信息摘要
     * 可空
     */
    private String errorMessage;

    /**
     * 元数据（JSONB）
     * 存储工具调用、引用来源等附加信息
     * ⚠️ V1 暂用 String，后续需要 MyBatis-Plus TypeHandler + FieldStrategy.NEVER
     */
    private String metadata;

    /**
     * 创建时间
     * INSERT 时由数据库自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Instant createdAt;

    /**
     * 最后修改时间
     * UPDATE 时由触发器自动维护
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}