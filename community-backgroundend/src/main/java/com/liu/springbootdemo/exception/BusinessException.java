package com.liu.springbootdemo.exception;

import com.liu.springbootdemo.common.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private String code;
    private String message;
    private HttpStatus httpStatus;
    private Object data;    //可选数据包


    public BusinessException(ErrorCode errorCode){

        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BusinessException(String mes, String code, HttpStatus httpStatus){
        super(mes);
        this.code = code;
        this.message = mes;
        this.httpStatus = httpStatus;
    }



}
