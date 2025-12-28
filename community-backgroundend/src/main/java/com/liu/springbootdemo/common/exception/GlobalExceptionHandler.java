package com.liu.springbootdemo.common.exception;

import com.liu.springbootdemo.POJO.Result.ErrorResponse;
import com.liu.springbootdemo.POJO.Result.Result;
import com.liu.springbootdemo.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

//BussinessException -> ErrorCode(enum) -> handler

/**
 * 全局异常处理器
 * 统一处理所有异常，返回标准化的错误响应
 *
 * @author Liu
 * @date 2025
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    /**
     * 处理业务异常 - 新的统一异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result> handleBusinessException(BusinessException e,
                                                         HttpServletRequest request) {
        String requestId = generateRequestId();

        log.warn("业务异常 [{}] - 路径: {}, 错误码: {}, 消息: {}",
                requestId, request.getRequestURI(), e.getCode(), e.getMessage());

        ErrorResponse error = ErrorResponse.full(
            e.getCode(),
            e.getMessage(),
            request.getRequestURI(),
            e.getData(),
            requestId
        );

        return ResponseEntity
                .status(e.getHttpStatus())
                .body(Result.error(error));
    }

    // ==================== Spring Validation 异常处理 ====================

    /**
     * 处理 @Valid 校验失败异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> handleValidationException(MethodArgumentNotValidException e,
                                                           HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("参数校验失败 - 路径: {}, 错误: {}", request.getRequestURI(), errors);

        ErrorResponse error = ErrorResponse.of(
            ErrorCode.PARAM_ERROR.getCode(),
            "参数校验失败",
            request.getRequestURI(),
            errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(error));
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result> handleBindException(BindException e,
                                                     HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        e.getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse error = ErrorResponse.of(
            ErrorCode.PARAM_ERROR.getCode(),
            "参数绑定失败",
            request.getRequestURI(),
            errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(error));
    }

    /**
     * 处理路径参数校验异常
     * 捕获加工例如，@Min(value = 18, message = "年龄必须大于18岁")
     * getMessage会得到包含message部分
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result> handleConstraintViolationException(ConstraintViolationException e,
                                                                    HttpServletRequest request) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();   //获取约束违例结果
        String message = violations.stream()
                .map(ConstraintViolation::getMessage)   //封装违规消息
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
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result> handleTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                             HttpServletRequest request) {
        String message = String.format("参数 '%s' 类型错误，期望类型: %s",
                e.getName(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        
        log.warn("参数类型不匹配 - 路径: {}, 错误: {}", request.getRequestURI(), message);

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
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result> handleMissingParameterException(MissingServletRequestParameterException e,
                                                                 HttpServletRequest request) {
        String message = String.format("缺少必要参数: %s", e.getParameterName());

        ErrorResponse error = ErrorResponse.of(
            ErrorCode.PARAM_MISSING.getCode(),
            message,
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

    // ==================== Spring Web 异常处理 ====================

    /**
     * 处理404 - 接口不存在
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result> handleNoHandlerFoundException(NoHandlerFoundException e,
                                                               HttpServletRequest request) {
        String message = String.format("接口不存在: %s %s",
                request.getMethod(), request.getRequestURL());

        ErrorResponse error = ErrorResponse.of(
            "404",
            message,
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Result.error(error));
    }

    /**
     * 处理请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                                   HttpServletRequest request) {
        String message = String.format("不支持 %s 请求方法，支持的方法: %s",
                e.getMethod(),
                e.getSupportedHttpMethods() != null ?
                    e.getSupportedHttpMethods().toString() : "未知");

        ErrorResponse error = ErrorResponse.of(
            "405",
            message,
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Result.error(error));
    }

    /**
     * 处理媒体类型不支持
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e,
                                                                      HttpServletRequest request) {
        String message = String.format("不支持的Content-Type: %s",
                e.getContentType());

        ErrorResponse error = ErrorResponse.of(
            "415",
            message,
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
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

    /**
     * 处理文件上传大小超限
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e,
                                                                      HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.of(
            ErrorCode.FILE_TOO_LARGE.getCode(),
            "文件大小超过限制",
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Result.error(error));
    }

    // ==================== 兜底异常处理 ====================

    /**
     * 处理其他未知异常 - 最后的防线
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception e, HttpServletRequest request) {
        String requestId = generateRequestId();

        // 记录详细错误日志
        log.error("系统异常 [{}] - 路径: {}, 方法: {}",
                requestId,
                request.getRequestURI(),
                request.getMethod(),
                e);

        // 构建错误响应
        ErrorResponse error = ErrorResponse.full(
            ErrorCode.SYSTEM_ERROR.getCode(),
            "系统繁忙，请稍后重试",
            request.getRequestURI(),
            null,
            requestId
        );

        // 开发环境返回详细错误信息
        if (isDevelopment()) {
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("exception", e.getClass().getSimpleName());
            debugInfo.put("message", e.getMessage());
            error.setData(debugInfo);
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(error));
    }

    /**
     * 生成请求ID用于追踪
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 判断是否为开发环境
     */
    private boolean isDevelopment() {
        return "dev".equals(activeProfile) || "development".equals(activeProfile);
    }
}