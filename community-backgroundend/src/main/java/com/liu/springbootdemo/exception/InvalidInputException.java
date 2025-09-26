package com.liu.springbootdemo.exception;


// 也可以用这个@ResponseStatus直接用spring将捕获该异常转化为指定https异常码
//@ResponseStatus(value = HttpStatus.CONFLICT)

/**
 * 无效输入
 * Bad_Request 400
 */
public class InvalidInputException extends RuntimeException{
    public InvalidInputException(String message) {
        super(message);
    }
}
