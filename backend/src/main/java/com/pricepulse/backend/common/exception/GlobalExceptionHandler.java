package com.pricepulse.backend.common.exception;

import com.pricepulse.backend.common.response.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理其他未预期异常（开发阶段可打印堆栈）
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleUnexpectedException(Exception e) {
        return Result.error(500, "服务器内部错误");
    }
}