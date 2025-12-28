package com.liu.springbootdemo.common.exception;

import com.liu.springbootdemo.common.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    private String code;
    private String message;
    private HttpStatus httpStatus;
    private Object data;    //可选数据包


    //单ErrorCode构造
    public BusinessException(ErrorCode errorCode){

        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }
    //Errorcode构造加自定义报错消息
    public BusinessException(ErrorCode errorCode, String message){
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    //ErrorCode加data
    public BusinessException(ErrorCode errorCode, Object data){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();;
        this.message = errorCode.getMessage();;
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    //ErrorCode加data加自定义消息
    public BusinessException(ErrorCode errorCode, String message, Object data){
        super(message);
        this.code = errorCode.getCode();;
        this.message = message;;
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    //异常链，不知道是什么场景会调用
    public BusinessException(ErrorCode errorCode, Throwable cause){
        super(errorCode.getMessage(),cause);
        this.code = errorCode.getCode();;
        this.message = errorCode.getMessage();;
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    //异常链，不知道是什么场景会调用
    public BusinessException(ErrorCode errorCode, String message, Throwable cause){
        super(message,cause);
        this.code = errorCode.getCode();;
        this.message = errorCode.getMessage();;
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    //TODO:静态工厂方法，AI给的，不理解，没用到

    /**
     * 静态工厂方法 - 创建带格式化消息的异常
     *
     * @param errorCode 错误码枚举
     * @param args      格式化参数
     * @return BusinessException
     */
    public static com.liu.springbootdemo.common.exception.BusinessException of(ErrorCode errorCode, Object... args) {
        String message = String.format(errorCode.getMessage(), args);
        return new com.liu.springbootdemo.common.exception.BusinessException(errorCode, message);
    }

    /**
     * 静态工厂方法 - 创建带自定义消息格式的异常
     *
     * @param errorCode     错误码枚举
     * @param messageFormat 消息格式
     * @param args          格式化参数
     * @return BusinessException
     */
    public static com.liu.springbootdemo.common.exception.BusinessException format(ErrorCode errorCode, String messageFormat, Object... args) {
        String message = String.format(messageFormat, args);
        return new com.liu.springbootdemo.common.exception.BusinessException(errorCode, message);
    }


}
