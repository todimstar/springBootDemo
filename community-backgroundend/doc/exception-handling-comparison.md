# 异常处理对比：旧版 vs 新版

## 快速对比表

| 特性 | 旧版（现有代码） | 新版（推荐方案） |
|------|-----------------|-----------------|
| **异常类数量** | 6个独立异常类 + 1个未完成的BizException | 1个统一BusinessException + 错误码枚举 |
| **错误码管理** | 无统一错误码 | ErrorCode枚举集中管理 |
| **HTTP状态码** | 在GlobalExceptionHandler中硬编码 | 在ErrorCode枚举中定义 |
| **错误信息** | 散落在各处，难以维护 | 集中在ErrorCode，支持国际化 |
| **异常处理** | 每个异常类型单独处理 | 统一处理BusinessException |
| **可扩展性** | 添加新异常需要新建类 | 只需在ErrorCode添加枚举值 |
| **前后端对接** | 无标准错误码，对接困难 | 标准错误码，对接清晰 |

## 代码对比示例

### 场景1：用户名已存在

#### 旧版代码
```java
// 1. 定义异常类
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

// 2. Service层抛出异常
if (existingUser != null) {
    throw new UserAlreadyExistsException("用户名已被占用！");
}

// 3. GlobalExceptionHandler处理
@ExceptionHandler(UserAlreadyExistsException.class)
public ResponseEntity<Result> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Result.error(e.getMessage()));
}

// 前端收到的响应
{
    "code": null,  // 没有错误码
    "message": "用户名已被占用！",
    "data": null
}
```

#### 新版代码
```java
// 1. 在ErrorCode枚举中定义
USERNAME_EXISTS("10101", "用户名已被占用", HttpStatus.CONFLICT),

// 2. Service层抛出异常（多种方式）
// 方式1：使用默认消息
throw new BusinessException(ErrorCode.USERNAME_EXISTS);

// 方式2：自定义消息
throw new BusinessException(ErrorCode.USERNAME_EXISTS,
    String.format("用户名 '%s' 已被占用", username));

// 方式3：使用断言工具
Assert.isNull(existingUser, ErrorCode.USERNAME_EXISTS);

// 3. GlobalExceptionHandler统一处理所有BusinessException

// 前端收到的响应
{
    "code": 200,
    "message": "error",
    "data": {
        "code": "10101",  // 标准错误码
        "message": "用户名 'john' 已被占用",
        "path": "/api/auth/register",
        "timestamp": "2024-01-20 10:30:45",
        "requestId": "a1b2c3d4e5f6"
    }
}
```

### 场景2：参数校验

#### 旧版代码
```java
// Controller或Service中手动校验
if (!StringUtils.hasText(user.getUsername())) {
    throw new InvalidInputException("请输入用户名");
}
if (!StringUtils.hasText(user.getEmail())) {
    throw new InvalidInputException("请输入邮箱?");
}
if (!StringUtils.hasText(user.getPassword())) {
    throw new InvalidInputException("请输入密码");
}
if (user.getPassword().length() < 5 || user.getPassword().length() > 20) {
    throw new InvalidInputException("密码长度必须在5到20个字符之间");
}
```

#### 新版代码
```java
// 使用断言工具类，代码更简洁
Assert.hasText(user.getUsername(), ErrorCode.PARAM_MISSING, "用户名不能为空");
Assert.hasText(user.getEmail(), ErrorCode.PARAM_MISSING, "邮箱不能为空");
Assert.hasText(user.getPassword(), ErrorCode.PARAM_MISSING, "密码不能为空");
Assert.lengthBetween(user.getPassword(), 6, 20, ErrorCode.PASSWORD_TOO_SHORT);
Assert.isEmail(user.getEmail(), ErrorCode.EMAIL_INVALID);

// 或者使用 @Valid 注解（推荐）
public class UserRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;
}

// Controller中
public Result<User> register(@Valid @RequestBody UserRegisterDTO dto) {
    // 校验自动完成，失败会抛出 MethodArgumentNotValidException
    // 由全局异常处理器统一处理
}
```

### 场景3：复杂业务异常

#### 旧版代码
```java
// CategoryServiceImpl - 混用BizException和字符串消息
if (existingCategory != null) {
    throw new BizException("分区名称已存在");
}

if (countFromPosts > 0) {
    throw new BizException("该分区下还有" + countFromPosts + "个帖子，无法删除");
}

// 没有标准的错误处理
```

#### 新版代码
```java
// 带上下文信息的异常
if (postCount > 0) {
    // 构建详细的错误信息
    Map<String, Object> details = new HashMap<>();
    details.put("categoryId", categoryId);
    details.put("categoryName", category.getName());
    details.put("postCount", postCount);

    throw new BusinessException(
        ErrorCode.CATEGORY_HAS_POSTS,
        String.format("分区 '%s' 下还有 %d 个帖子，请先处理这些帖子",
            category.getName(), postCount),
        details
    );
}

// 前端收到的响应包含详细信息
{
    "code": 200,
    "message": "error",
    "data": {
        "code": "40203",
        "message": "分区 '技术讨论' 下还有 15 个帖子，请先处理这些帖子",
        "path": "/api/categories/1",
        "timestamp": "2024-01-20 10:30:45",
        "data": {
            "categoryId": 1,
            "categoryName": "技术讨论",
            "postCount": 15
        }
    }
}
```

## 迁移难度评估

### 简单迁移（5分钟/个）
- InvalidInputException → ErrorCode.PARAM_ERROR
- UnauthorizedException → ErrorCode.UNAUTHORIZED
- NotAuthorException → ErrorCode.FORBIDDEN

### 中等迁移（10分钟/个）
- NotFindException → 需要区分是帖子、用户还是评论
- UserAlreadyExistsException → 需要区分用户名还是邮箱

### 复杂迁移（15分钟/个）
- BizException → 需要分析具体业务场景，创建对应的ErrorCode

## 性能对比

| 指标 | 旧版 | 新版 |
|------|------|------|
| **异常创建开销** | 每个异常类都需要加载 | 统一使用BusinessException，减少类加载 |
| **内存占用** | 多个异常类占用更多内存 | 单一异常类，内存占用少 |
| **维护成本** | 高，需要维护多个类 | 低，只需维护ErrorCode枚举 |
| **查找效率** | 分散在多个文件，查找困难 | 集中在一个枚举，查找方便 |

## 前端对接优势

### 旧版问题
```javascript
// 前端需要根据message判断错误类型（不可靠）
if (error.message.includes('已被占用')) {
    // 处理用户名重复
} else if (error.message.includes('不存在')) {
    // 处理资源不存在
}
// message可能变化，导致前端逻辑失效
```

### 新版优势
```javascript
// 前端根据标准错误码处理（可靠）
switch (error.data.code) {
    case '10101':  // USERNAME_EXISTS
        showError('该用户名已被使用，请换一个');
        highlightField('username');
        break;
    case '10102':  // EMAIL_EXISTS
        showError('该邮箱已被注册');
        highlightField('email');
        break;
    case '40001':  // POST_NOT_FOUND
        showError('帖子不存在');
        redirectToHomePage();
        break;
    default:
        showError(error.data.message);
}

// 错误码不会变化，前端逻辑稳定
```

## 国际化支持

### 旧版：不支持
```java
throw new InvalidInputException("请输入用户名");  // 硬编码中文
```

### 新版：易于支持
```java
// ErrorCode枚举的message可以是资源key
USERNAME_MISSING("10001", "error.username.missing", HttpStatus.BAD_REQUEST),

// 配合MessageSource实现国际化
@Autowired
private MessageSource messageSource;

String message = messageSource.getMessage(
    errorCode.getMessage(),
    null,
    LocaleContextHolder.getLocale()
);

// messages_zh_CN.properties
error.username.missing=请输入用户名

// messages_en_US.properties
error.username.missing=Please enter username
```

## 总结

### 新版异常处理的优势

1. **统一管理**：所有错误码集中管理，便于维护
2. **标准化**：前后端使用标准错误码对接，减少沟通成本
3. **可扩展**：添加新错误只需添加枚举值，无需新建类
4. **性能更好**：减少类加载，降低内存占用
5. **支持国际化**：易于实现多语言支持
6. **调试友好**：包含请求ID、时间戳等调试信息
7. **代码更简洁**：使用断言工具类，减少样板代码

### 推荐迁移策略

1. **保持兼容**：新的GlobalExceptionHandler兼容旧异常
2. **逐步迁移**：一个Service一个Service地迁移
3. **测试驱动**：先写测试，确保迁移不破坏功能
4. **文档先行**：先定义ErrorCode，再开始迁移

这种重构不仅能提升代码质量，还能显著改善开发体验和维护效率。