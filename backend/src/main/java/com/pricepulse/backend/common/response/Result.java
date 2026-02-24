package com.pricepulse.backend.common.response;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果
 * @param <T>
 */

@Data
@NoArgsConstructor
public class Result<T> {

    private Integer code;    // 状态码：200 成功，400 参数错误，500 异常等
    private String message;  // 提示信息
    private T data;          // 返回数据

    // 成功返回（无数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 成功返回（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功返回（自定义消息 + 数据）
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // 失败返回（自定义错误码和消息）
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // 通用失败（如业务异常）
    public static <T> Result<T> error(String message) {
        return new Result<>(400, message, null);
    }

    // 私有构造器
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}