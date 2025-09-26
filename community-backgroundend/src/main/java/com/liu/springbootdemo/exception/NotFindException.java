package com.liu.springbootdemo.exception;

/**
 * 服务器没有资源，404
 */
public class NotFindException extends RuntimeException{
    public NotFindException(String message) {
        super(message);
    }
}
