package com.liu.springbootdemo.exception;

/**
 * 不是帖子作者，ForBidden，403
 */
public class NotAuthorException extends RuntimeException{
    public NotAuthorException(String message) {
        super(message);
    }
}
