package com.keyx.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * 用于业务代码主动抛出错误，例如：
 * <ul>
 *   <li>用户名已存在</li>
 *   <li>密码错误</li>
 *   <li>账号已禁用</li>
 *   <li>Token 已过期</li>
 * </ul>
 *
 * 与 @Valid 注解的入参校验不同：
 * <ul>
 *   <li>@Valid：检查"格式对不对"（请求进来时检查）</li>
 *   <li>BusinessException：检查"业务通不通"（运行时检查）</li>
 * </ul>
 *
 * 使用示例（在 Service 方法体内）：
 * <pre>
 *   throw new BusinessException("用户名已存在");                  // 默认 code = 400
 *   throw new BusinessException(409, "用户名已存在");            // 自定义 code = 409
 *   throw new BusinessException(401, "Token 已过期");           // 自定义 code = 401
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码
     * 默认 400（客户端错误）
     * 也可以是 401（未授权）、403（禁止）、404（未找到）、423（锁定）、501（未实现）等
     */
    private final Integer code;

    /**
     * 构造方法 1：只传 message，code 默认 400
     *
     * @param message 错误信息，会返回给前端
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 构造方法 2：自定义 code + message
     *
     * @param code    业务错误码
     * @param message 错误信息，会返回给前端
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}