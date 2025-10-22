package com.liu.springbootdemo.exception;

import com.liu.springbootdemo.POJO.vo.ErrorResponse;
import com.liu.springbootdemo.POJO.vo.Result;
import com.liu.springbootdemo.common.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;
import java.util.stream.Collectors;

// BizException - > ErrorCode(enum)

// Exception

@Slf4j
@RestControllerAdvice   //声明是全局异常处理类
public class GlobalExceptionHandler {

    /**
     * 处理业务异常 - 业务统一异常
     * @param e
     * @param request
     * @return
     */
     @ExceptionHandler(BusinessException.class)
     public ResponseEntity<Result> handleBizException(BusinessException e,
                                                      HttpServletRequest request){
//         String requestId = generateRequestId();  //UUID的生成请求ID

         log.warn("业务异常[] - 路径: {}, 错误码: {}, 消息: {}",
                 request.getRequestURI(),e.getCode(),e.getMessage());

         ErrorResponse error = ErrorResponse.of(
                 e.getCode(),
                 e.getMessage(),
                 request.getRequestURI(),
                 e.getData()
         );

         return ResponseEntity
                 .status(e.getHttpStatus())
                 .body(Result.error(error));
     }

    // ======================= Spring Validation 异常处理 ===========================

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result> handleTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                              HttpServletRequest request) {
        String message = String.format("参数 '%s' 类型错误，期望类型: %s",
                e.getName(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.PARAM_TYPE_ERROR.getCode(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(error));
    }

    /**
     * 处理路径参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result> handleConstraintViolationException(ConstraintViolationException e,
                                                                     HttpServletRequest request) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.PARAM_ERROR.getCode(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(error));
    }

    /**
     * 处理请求体读取异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result> handleMessageNotReadableException(HttpMessageNotReadableException e,
                                                                    HttpServletRequest request) {
        log.warn("请求体解析失败 - 路径: {}", request.getRequestURI(), e);

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.PARAM_FORMAT_ERROR.getCode(),
                "请求体格式错误，请检查JSON格式",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(error));
    }


    // ==================== Spring Security 异常处理 ====================

    /**
     * 处理Spring Security权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result> handleAccessDeniedException(AccessDeniedException e,
                                                              HttpServletRequest request) {
        log.warn("权限不足 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.FORBIDDEN.getCode(),
                "权限不足，无法访问该资源",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Result.error(error));
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result> handleAuthenticationException(AuthenticationException e,
                                                                HttpServletRequest request) {
        String code = ErrorCode.UNAUTHORIZED.getCode();
        String message = "认证失败";

        if (e instanceof BadCredentialsException) {
            code = ErrorCode.WRONG_PASSWORD.getCode();
            message = "用户名或密码错误";
        } else if (e instanceof InsufficientAuthenticationException) {
            code = ErrorCode.TOKEN_MISSING.getCode();
            message = "请先登录";
        }

        ErrorResponse error = ErrorResponse.of(code, message, request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(error));
    }


     //==============================保留旧异常=========================

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
