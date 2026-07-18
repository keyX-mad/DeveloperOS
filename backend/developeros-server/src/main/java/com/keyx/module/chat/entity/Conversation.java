package com.keyx.module.chat.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 会话实体
 *
 * 对应数据库表：conversation
 * （一次 AI 对话 = 一个 conversation，里面有多条 message）
 *
 * 数据库字段对照（Java camelCase → DB snake_case 由 MyBatis-Plus 自动映射）：
 *   id              BIGINT       PRIMARY KEY
 *   user_id         BIGINT       所属用户
 *   title           VARCHAR(200) 会话标题（默认 "New conversation"）
 *   model_name      VARCHAR(100) 会话使用的模型
 *   status          VARCHAR(20)  状态：active / archived
 *   last_message_at TIMESTAMPTZ  最后一条消息时间（派生字段，Service 维护）
 *   created_at      TIMESTAMPTZ  创建时间
 *   updated_at      TIMESTAMPTZ  最后修改时间
 */
@Data
@TableName("conversation")
public class Conversation {

    /**
     * 会话 ID（主键，数据库自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户 ID
     * 外键关联 app_user.id，但 V1 实体不维护 User 引用（按需查询）
     */
    private Long userId;

    /**
     * 会话标题
     * 默认 "New conversation"，可由首条消息或 AI 生成后修改
     */
    private String title;

    /**
     * 会话使用的模型名称
     * 例如：gpt-4o-mini、deepseek-v4-flash
     * 可空（用户没选模型时用默认）
     */
    private String modelName;

    /**
     * 会话状态
     * 数据库用 VARCHAR + CHECK 约束
     * 值：active（正常）/ archived（归档）
     * V1 暂用 String，后续可改为枚举
     */
    private String status;

    /**
     * 最后一条消息时间（派生字段）
     * ⚠️ Service 层在保存消息时必须在同一事务更新它
     * 用于"最近会话"排序
     */
    private Instant lastMessageAt;

    /**
     * 创建时间
     * 数据库 DEFAULT CURRENT_TIMESTAMP，INSERT 时由数据库填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Instant createdAt;

    /**
     * 最后修改时间
     * 数据库有触发器自动维护（set_updated_at）
     * 这里加注解是为了字段语义清晰
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}