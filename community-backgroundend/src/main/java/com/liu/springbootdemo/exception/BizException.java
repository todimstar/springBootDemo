package com.liu.springbootdemo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class BizException extends RuntimeException {

    @Getter
    @AllArgsConstructor
    public static enum ErrorCode{
        UNAUTH("asdsa","dsada"),

        ;
        private String code;
        private String mes;

    }

    public BizException(ErrorCode errorCode){
        super(errorCode.getMes());
    }

    public BizException(String mes, String code, HttpStatus httpStatus){
        super(mes);

    }



}
