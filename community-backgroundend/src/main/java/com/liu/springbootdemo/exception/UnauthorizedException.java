package com.liu.springbootdemo.exception;

/**
 * 未认证，401
 */
public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(String message) {
        super(message);
    }
}
