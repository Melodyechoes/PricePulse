package com.pricepulse.backend.common.exception;

/**
 * 业务异常
 * 用于抛出明确的业务逻辑错误（如用户名已存在、权限不足等）
 */

public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400; // 默认 400 Bad Request
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}