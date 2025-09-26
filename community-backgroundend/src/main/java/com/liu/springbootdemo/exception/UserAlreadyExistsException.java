package com.liu.springbootdemo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 当尝试创建已存在的用户时，抛出此异常
// @ResponseStatus 注解可以让Spring直接将此异常转换为指定的HTTP状态码，是一种简化方法
// 但为了在GlobalExceptionHandler中进行更灵活的处理，我们暂时不使用它
// @ResponseStatus(value = HttpStatus.CONFLICT)

/**
 * 用户已存在，409
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
