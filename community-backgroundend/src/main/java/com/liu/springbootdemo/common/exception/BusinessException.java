package com.liu.springbootdemo.common.exception;

import com.liu.springbootdemo.common.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 统一业务异常类
 * 所有业务异常都应该使用这个类或其子类
 *
 * @author Liu
 * @date 2024
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * HTTP状态码
     */
    private final HttpStatus httpStatus;

    /**
     * 附加数据（可选）
     */
    private final Object data;

    /**
     * 构造函数 - 使用错误码枚举
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * 构造函数 - 使用错误码枚举和自定义消息
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * 构造函数 - 使用错误码枚举和附加数据
     *
     * @param errorCode 错误码枚举
     * @param data      附加数据
     */
    public BusinessException(ErrorCode errorCode, Object data) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    /**
     * 构造函数 - 使用错误码枚举、自定义消息和附加数据
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     * @param data      附加数据
     */
    public BusinessException(ErrorCode errorCode, String message, Object data) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    /**
     * 构造函数 - 支持异常链
     *
     * @param errorCode 错误码枚举
     * @param cause     原始异常
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * 构造函数 - 支持异常链和自定义消息
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     * @param cause     原始异常
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
        this.message = message;
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * 静态工厂方法 - 创建带格式化消息的异常
     *
     * @param errorCode 错误码枚举
     * @param args      格式化参数
     * @return BusinessException
     */
    public static BusinessException of(ErrorCode errorCode, Object... args) {
        String message = String.format(errorCode.getMessage(), args);
        return new BusinessException(errorCode, message);
    }

    /**
     * 静态工厂方法 - 创建带自定义消息格式的异常
     *
     * @param errorCode     错误码枚举
     * @param messageFormat 消息格式
     * @param args          格式化参数
     * @return BusinessException
     */
    public static BusinessException format(ErrorCode errorCode, String messageFormat, Object... args) {
        String message = String.format(messageFormat, args);
        return new BusinessException(errorCode, message);
    }
}