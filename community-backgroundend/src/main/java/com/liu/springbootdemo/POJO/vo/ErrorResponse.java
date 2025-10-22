package com.liu.springbootdemo.POJO.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一错误响应格式
 * 用于返回详细的错误信息
 *
 * @author Liu
 * @date 2024
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // null字段不序列化
public class ErrorResponse {

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 额外数据（如校验失败的字段详情）
     */
    private Object data;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 静态工厂方法 - 创建基本错误响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param path    请求路径
     * @return ErrorResponse
     */
    public static ErrorResponse of(String code, String message, String path) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 静态工厂方法 - 创建带附加数据的错误响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param path    请求路径
     * @param data    附加数据
     * @return ErrorResponse
     */
    public static ErrorResponse of(String code, String message, String path, Object data) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(path)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 静态工厂方法 - 创建完整的错误响应
     *
     * @param code      错误码
     * @param message   错误消息
     * @param path      请求路径
     * @param data      附加数据
     * @param requestId 请求ID
     * @return ErrorResponse
     */
    public static ErrorResponse full(String code, String message, String path, Object data, String requestId) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(path)
                .data(data)
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .build();
    }
}