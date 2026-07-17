package com.keyx.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * MyBatis-Plus 元数据处理器
 *
 * 作用：自动填充创建时间、更新时间等字段
 *
 * 对应实体类上的注解：
 * - @TableField(fill = FieldFill.INSERT)   插入时填充
 * - @TableField(fill = FieldFill.INSERT_UPDATE)  插入和更新时都填充
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Instant now = Instant.now();
        this.strictInsertFill(metaObject, "createdAt", Instant.class, now);
        this.strictInsertFill(metaObject, "updatedAt", Instant.class, now);
    }

    /**
     * 更新时填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", Instant.class, Instant.now());
    }
}