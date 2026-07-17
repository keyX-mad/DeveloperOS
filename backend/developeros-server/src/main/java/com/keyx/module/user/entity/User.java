package com.keyx.module.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 用户实体
 *
 * 对应数据库表：app_user
 * （不是 user，因为 USER 在 PostgreSQL 中是关键字）
 *
 * 字段说明详见 V1__init_schema.sql 和 Database Design.md
 */
@Data
@TableName("app_user")
public class User {

    /**
     * 用户 ID
     * 数据库使用 IDENTITY 自增，对应 IdType.AUTO
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码哈希（BCrypt 加密后的字符串）
     * 绝不存储明文密码
     */
    private String passwordHash;

    /**
     * 昵称（页面展示用）
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatarUrl;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 时区，默认 Asia/Shanghai
     */
    private String timezone;

    /**
     * 用户偏好（JSONB）
     * ⚠️ V1 暂用 String，不参与 update（数据库默认值生效）
     * 后续需要 TypeHandler 处理 JSONB 类型
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String preferences;

    /**
     * 账号状态：active / disabled / locked
     * 数据库用 VARCHAR + CHECK 约束
     * V1 用 String，后续可改为 Enum
     */
    private String status;

    /**
     * 最近登录时间
     */
    private Instant lastLoginAt;

    /**
     * 创建时间
     * 数据库 DEFAULT CURRENT_TIMESTAMP
     * 插入时由数据库自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Instant createdAt;

    /**
     * 最后修改时间
     * 数据库有触发器自动维护（set_updated_at）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}