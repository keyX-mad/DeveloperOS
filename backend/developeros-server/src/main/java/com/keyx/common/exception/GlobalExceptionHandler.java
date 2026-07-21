package com.keyx.common.exception;

import com.keyx.common.R;
import com.keyx.module.chat.exception.ChatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ① Chat 模块异常（1500+ 段）
    @ExceptionHandler(ChatException.class)
    public R<?> handleChat(ChatException e) {
        log.warn("Chat 业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    // ② 业务异常（其他模块）
    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusiness(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    // ③ 参数校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数错误");
        log.warn("参数校验失败：{}", msg);
        return R.fail(400, msg);
    }

    // ④ 兜底
    @ExceptionHandler(Exception.class)
    public R<?> handleUnknown(Exception e) {
        log.error("系统异常", e);
        return R.fail(500, "服务器内部错误");
    }
}
