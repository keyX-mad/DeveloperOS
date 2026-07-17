package com.keyx.common;

import lombok.Data;

/**
 * 统一响应格式
 *
 * 所有 Controller 接口返回的数据都包装成这个格式：
 * {
 *   "code": 200,           // 状态码：200成功，4xx客户端错误，5xx服务端错误
 *   "message": "success",  // 提示信息
 *   "data": { ... }        // 业务数据（成功时有值，失败时为 null）
 * }
 *
 * 为什么需要：
 * 1. 前端对接简单 - 所有接口返回结构一致
 * 2. 错误统一处理 - code 非 200 就是失败，前端统一弹错误提示
 * 3. 业务数据安全 - data 可以是任何类型（User、List、String...）
 */
@Data
public class R<T> {

    /**
     * 状态码：200 成功，其他失败
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 私有构造，避免外部直接 new
     */
    private R() {
    }

    /**
     * 成功响应（无数据）
     * 用法：return R.ok();
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应（带数据）
     * 用法：return R.ok(user);
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    /**
     * 失败响应
     * 用法：return R.fail(400, "用户名已存在");
     */
    public static <T> R<T> fail(Integer code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        r.data = null;
        return r;
    }

    /**
     * 失败响应（默认 400 错误）
     * 用法：return R.fail("用户名已存在");
     */
    public static <T> R<T> fail(String message) {
        return fail(400, message);
    }
}