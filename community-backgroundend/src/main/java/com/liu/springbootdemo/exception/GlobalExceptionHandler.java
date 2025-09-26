package com.liu.springbootdemo.exception;

import com.liu.springbootdemo.entity.VO.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// BizException - > ErrorCode(enum)

// Exception

@RestControllerAdvice   //声明是全局异常处理类
public class GlobalExceptionHandler {

    // @ExceptionHandler(updateLogintimeException.class)
    // public Result handleUpdateLogintimeException(updateLogintimeException e){
    //     return new Result<>(1001,"更新登录时间失败",+e.getMessage(),null);
    // }

    // 处理"用户名或密码错误"/"认证过期"异常，认证失败，服務器不知道你是誰，返回401
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result> handleUnauthorizedException(UnauthorizedException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(e.getMessage()));
    }

    // 处理不是作者的查看、修改操作，服务器知道你是谁，但是拒绝操作，返回403
    @ExceptionHandler(NotAuthorException.class)
    public ResponseEntity<Result> handleNotPostAuthorException(NotAuthorException e){
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)//ForBidden
                .body(Result.error(e.getMessage()));
    }

    // 处理没找到帖子或者其他资源的情况，服务器中没有想要的资源，返回经典404
    @ExceptionHandler(NotFindException.class)
    public ResponseEntity<Result> handleNotFindException(NotFindException e){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Result.error(e.getMessage()));
    }

    // 专门处理“输入不合法”的异常，客戶端輸入有問題，返回400
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Result> handleInvalidInputException(InvalidInputException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(e.getMessage()));
    }

    // 新增：专门处理“用户信息已存在”的异常,返回409，
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Result> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        // 返回 HTTP 409 Conflict 状态码
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Result.error(e.getMessage()));
    }

    // 最后一道防线：处理所有其他未被捕获的异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception e){
        // 在服务器后台打印完整的错误堆栈信息，用于调试
        e.printStackTrace(); 
        
        // 向客户端返回一个通用的、模糊的错误信息和 HTTP 500 状态码
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("服务器内部错误，请稍后重试"+e.getMessage()));
    }
}
