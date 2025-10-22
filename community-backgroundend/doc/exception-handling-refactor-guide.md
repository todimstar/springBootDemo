# Spring Boot 异常处理重构方案

## 现代 Spring Boot 异常处理最佳实践

### 核心原则

1. **统一异常体系**：使用单一基类 + 错误码枚举
2. **语义化HTTP状态码**：正确映射业务异常到HTTP状态
3. **结构化错误信息**：包含错误码、消息、时间戳、请求路径
4. **异常链追踪**：保留原始异常信息用于调试
5. **国际化支持**：错误消息支持多语言
6. **性能考虑**：避免在异常中做重操作

## 推荐架构方案

### 方案一：错误码驱动（推荐）

这是大型项目的最佳实践，类似阿里巴巴、美团的做法。

```java
// 1. 错误码枚举 - 所有错误的中央管理
package com.liu.springbootdemo.exception;

import lombok.Getter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // ========== 通用错误码 ==========
    SUCCESS("00000", "success", HttpStatus.OK),
    SYSTEM_ERROR("A0001", "系统执行出错", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_BUSY("A0002", "系统繁忙，请稍后重试", HttpStatus.SERVICE_UNAVAILABLE),

    // ========== 用户相关错误码 10001-19999 ==========
    USER_NOT_FOUND("10001", "用户不存在", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("10002", "用户已存在", HttpStatus.CONFLICT),
    USERNAME_EXISTS("10003", "用户名已被占用", HttpStatus.CONFLICT),
    EMAIL_EXISTS("10004", "邮箱已被注册", HttpStatus.CONFLICT),

    // ========== 认证授权错误码 20001-29999 ==========
    UNAUTHORIZED("20001", "未登录或登录已过期", HttpStatus.UNAUTHORIZED),
    WRONG_PASSWORD("20002", "用户名或密码错误", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("20003", "登录已过期，请重新登录", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("20004", "无效的登录凭证", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("20005", "无权限访问", HttpStatus.FORBIDDEN),

    // ========== 参数校验错误码 30001-39999 ==========
    PARAM_ERROR("30001", "参数错误", HttpStatus.BAD_REQUEST),
    PARAM_MISSING("30002", "缺少必要参数", HttpStatus.BAD_REQUEST),
    PARAM_TYPE_ERROR("30003", "参数类型错误", HttpStatus.BAD_REQUEST),
    PARAM_FORMAT_ERROR("30004", "参数格式错误", HttpStatus.BAD_REQUEST),

    // ========== 业务错误码 40001-49999 ==========
    // 帖子相关 40001-40999
    POST_NOT_FOUND("40001", "帖子不存在", HttpStatus.NOT_FOUND),
    POST_NOT_AUTHOR("40002", "您不是该帖子的作者", HttpStatus.FORBIDDEN),
    POST_TITLE_EMPTY("40003", "帖子标题不能为空", HttpStatus.BAD_REQUEST),
    POST_CONTENT_EMPTY("40004", "帖子内容不能为空", HttpStatus.BAD_REQUEST),

    // 评论相关 41001-41999
    COMMENT_NOT_FOUND("41001", "评论不存在", HttpStatus.NOT_FOUND),
    COMMENT_NOT_AUTHOR("41002", "您不是该评论的作者", HttpStatus.FORBIDDEN),
    COMMENT_CONTENT_EMPTY("41003", "评论内容不能为空", HttpStatus.BAD_REQUEST),

    // 分区相关 42001-42999
    CATEGORY_NOT_FOUND("42001", "分区不存在", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_EXISTS("42002", "分区名称已存在", HttpStatus.CONFLICT),
    CATEGORY_HAS_POSTS("42003", "分区下还有帖子，无法删除", HttpStatus.CONFLICT),
    CATEGORY_CREATE_FAILED("42004", "创建分区失败", HttpStatus.INTERNAL_SERVER_ERROR),

    // ========== 第三方服务错误码 50001-59999 ==========
    UPLOAD_FILE_ERROR("50001", "文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR),
    SMS_SEND_ERROR("50002", "短信发送失败", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_SEND_ERROR("50003", "邮件发送失败", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
```

```java
// 2. 统一业务异常类
package com.liu.springbootdemo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    private final Object data;  // 可选的附加数据

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;  // 自定义消息
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    public BusinessException(ErrorCode errorCode, Object data) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;  // 附加数据，如具体的错误字段
    }

    public BusinessException(ErrorCode errorCode, String message, Object data) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    // 支持异常链
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }
}
```

```java
// 3. 统一错误响应格式
package com.liu.springbootdemo.POJO.vo;

import lombok.Data;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // null字段不序列化
public class ErrorResponse {

    private String code;        // 错误码
    private String message;     // 错误消息
    private String path;        // 请求路径
    private LocalDateTime timestamp;  // 时间戳
    private Object data;        // 额外数据（如校验失败的字段）

    // 便捷构造方法
    public static ErrorResponse of(String code, String message, String path) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(String code, String message, String path, Object data) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(path)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

```java
// 4. 全局异常处理器
package com.liu.springbootdemo.exception;

import com.liu.springbootdemo.POJO.vo.ErrorResponse;
import com.liu.springbootdemo.POJO.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result> handleBusinessException(BusinessException e,
                                                         HttpServletRequest request) {
        log.error("业务异常 - 路径: {}, 错误码: {}, 消息: {}",
                  request.getRequestURI(), e.getCode(), e.getMessage());

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

    /**
     * 处理参数校验异常 - @Valid 校验失败
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
            errors  // 具体哪些字段校验失败
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
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result> handleConstraintViolationException(ConstraintViolationException e,
                                                                    HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.of(
            ErrorCode.PARAM_ERROR.getCode(),
            e.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(error));
    }

    /**
     * 处理参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result> handleTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                             HttpServletRequest request) {
        String message = String.format("参数 '%s' 类型错误，期望类型: %s",
                e.getName(), e.getRequiredType().getSimpleName());

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
     * 处理Spring Security权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result> handleAccessDeniedException(AccessDeniedException e,
                                                             HttpServletRequest request) {
        log.warn("权限不足 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());

        ErrorResponse error = ErrorResponse.of(
            ErrorCode.FORBIDDEN.getCode(),
            "权限不足",
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Result.error(error));
    }

    /**
     * 处理其他未知异常 - 最后的防线
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception e,
                                                 HttpServletRequest request) {
        log.error("系统异常 - 路径: {}", request.getRequestURI(), e);

        ErrorResponse error = ErrorResponse.of(
            ErrorCode.SYSTEM_ERROR.getCode(),
            "系统繁忙，请稍后重试",  // 不暴露内部错误信息
            request.getRequestURI()
        );

        // 生产环境不要返回具体错误信息
        if (isDebugMode()) {
            error.setData(e.getMessage());  // 开发环境可以返回具体错误
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(error));
    }

    private boolean isDebugMode() {
        // 可以通过配置文件控制
        return "dev".equals(System.getProperty("spring.profiles.active"));
    }
}
```

### 方案二：异常类继承体系（简化版）

如果您觉得方案一太复杂，这是一个简化但仍然专业的方案：

```java
// 1. 基础业务异常
package com.liu.springbootdemo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {

    private final HttpStatus status;

    public BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BaseException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}

// 2. 具体异常类
@Getter
public class NotFoundException extends BaseException {
    private final String resourceName;
    private final Object resourceId;

    public NotFoundException(String resourceName, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceName, resourceId),
              HttpStatus.NOT_FOUND);
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }
}

@Getter
public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

@Getter
public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}

@Getter
public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

@Getter
public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
```

## 使用示例

### 改造前（您当前的代码）
```java
// UserServiceImpl.java
if (existingUser != null) {
    throw new UserAlreadyExistsException("用户名已被占用！");
}

// CategoryServiceImpl.java
if (existingCategory != null) {
    throw new BizException("分区名称已存在");
}
```

### 改造后（使用方案一）
```java
// UserServiceImpl.java
if (existingUser != null) {
    throw new BusinessException(ErrorCode.USERNAME_EXISTS);
}

// 或者自定义消息
if (existingUser != null) {
    throw new BusinessException(
        ErrorCode.USERNAME_EXISTS,
        String.format("用户名 '%s' 已被占用", username)
    );
}

// CategoryServiceImpl.java
if (existingCategory != null) {
    throw new BusinessException(ErrorCode.CATEGORY_NAME_EXISTS);
}

// 带附加数据
if (category.getPostCount() > 0) {
    Map<String, Object> data = new HashMap<>();
    data.put("postCount", category.getPostCount());
    data.put("categoryId", category.getId());

    throw new BusinessException(
        ErrorCode.CATEGORY_HAS_POSTS,
        String.format("分区下还有 %d 个帖子，无法删除", category.getPostCount()),
        data
    );
}
```

## 异常处理最佳实践

### 1. 异常分层原则

```java
// Controller层 - 不要捕获异常，让全局处理器处理
@PostMapping("/posts")
public Result<Post> createPost(@Valid @RequestBody PostDTO postDTO) {
    // 不要try-catch，直接抛出
    return Result.success(postService.createPost(postDTO));
}

// Service层 - 只捕获需要转换的异常
@Transactional
public Post createPost(PostDTO postDTO) {
    try {
        // 业务逻辑
        return postMapper.insert(post);
    } catch (DataIntegrityViolationException e) {
        // 转换数据库异常为业务异常
        log.error("创建帖子失败，数据库约束冲突", e);
        throw new BusinessException(ErrorCode.POST_CREATE_FAILED, e);
    }
}

// Mapper层 - 不处理异常
Post insert(Post post);
```

### 2. 异常消息原则

```java
// ❌ 不好的做法
throw new RuntimeException("错误");
throw new RuntimeException("失败了");

// ✅ 好的做法
throw new BusinessException(ErrorCode.USER_NOT_FOUND);
throw new BusinessException(
    ErrorCode.POST_NOT_AUTHOR,
    String.format("您不是帖子 '%s' 的作者", post.getTitle())
);
```

### 3. 日志记录原则

```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);

        if (user == null) {
            // 记录警告级别日志
            log.warn("登录失败 - 用户不存在: {}", username);
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 记录警告级别日志，不记录密码
            log.warn("登录失败 - 密码错误, 用户: {}", username);
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        // 成功日志
        log.info("用户登录成功: {}", username);
        return user;
    }
}
```

### 4. 断言工具类（可选）

```java
// 断言工具类，简化判断逻辑
public class Assert {

    public static void notNull(Object object, ErrorCode errorCode) {
        if (object == null) {
            throw new BusinessException(errorCode);
        }
    }

    public static void isTrue(boolean expression, ErrorCode errorCode) {
        if (!expression) {
            throw new BusinessException(errorCode);
        }
    }

    public static void notEmpty(String str, ErrorCode errorCode) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(errorCode);
        }
    }
}

// 使用示例
public Post getPost(Long postId) {
    Post post = postMapper.findById(postId);
    Assert.notNull(post, ErrorCode.POST_NOT_FOUND);
    return post;
}
```

## 配置建议

### application.yml
```yaml
spring:
  # 开发环境显示详细错误
  profiles:
    active: dev

  # 关闭默认错误页面
  mvc:
    throw-exception-if-no-handler-found: true
  web:
    resources:
      add-mappings: false

# 开发环境配置
---
spring:
  config:
    activate:
      on-profile: dev

server:
  error:
    include-message: always
    include-binding-errors: always
    include-stacktrace: on_param
    include-exception: false

# 生产环境配置
---
spring:
  config:
    activate:
      on-profile: prod

server:
  error:
    include-message: never
    include-binding-errors: never
    include-stacktrace: never
    include-exception: false
```

## 测试建议

```java
@SpringBootTest
@AutoConfigureMockMvc
class ExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("10001"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.data.timestamp").exists())
            .andExpect(jsonPath("$.data.path").value("/api/users/999999"));
    }

    @Test
    void testUnauthorized() throws Exception {
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("20001"));
    }

    @Test
    void testValidationError() throws Exception {
        String invalidPost = "{\"title\":\"\",\"content\":\"\"}";

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + getValidToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPost))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("30001"))
            .andExpect(jsonPath("$.data.errors").exists())
            .andExpect(jsonPath("$.data.errors.title").exists())
            .andExpect(jsonPath("$.data.errors.content").exists());
    }
}
```

## 迁移建议

1. **第一步**：创建新的异常体系（不要删除旧的）
2. **第二步**：更新GlobalExceptionHandler
3. **第三步**：逐个Service迁移到新异常
4. **第四步**：测试验证
5. **第五步**：删除旧的异常类

## 总结

推荐您使用**方案一（错误码驱动）**，因为：
- 更容易维护和管理错误
- 支持国际化
- 前后端对接更清晰
- 符合大厂实践

这种架构在阿里巴巴、美团、字节跳动等大厂都有应用，是经过验证的最佳实践。