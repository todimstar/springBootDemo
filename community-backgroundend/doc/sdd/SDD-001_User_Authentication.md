# 系统设计文档 (SDD) - 用户认证模块

**文档编号**: SDD-001
**模块名称**: User Authentication (用户认证)
**版本**: 1.0

## 1. 数据库设计

根据需求，`user` 表的最终设计如下。

**`user` 表**

| 字段名          | 类型           | 约束/备注                                  |
| --------------- | -------------- | ------------------------------------------ |
| `id`            | `BIGINT`       | 主键, 自增                                 |
| `username`      | `VARCHAR(50)`  | 非空, 唯一索引                             |
| `password`      | `VARCHAR(255)` | 非空, 存储BCrypt哈希值                     |
| `email`         | `VARCHAR(100)` | 唯一索引                                   |
| `create_time`   | `DATETIME`     | 账户创建时间, 默认`CURRENT_TIMESTAMP`      |
| `last_login_time`| `DATETIME`     | 最后登录时间, 可为空                       |
| `role`          | `VARCHAR(50)`  | 用户角色, 默认 `'ROLE_USER'`               |

## 2. API 接口设计

### 2.1 用户注册 (`POST /api/auth/register`)

- **功能**: 创建一个新用户。
- **请求体 (Request Body)**: `application/json`
  ```json
  {
    "username": "newUser",
    "password": "password123",
    "email": "user@example.com"
  }
  ```
- **成功响应 (Success Response)**: `HTTP 201 Created`（√）
  ```json
  {
    "code": 0,
    "message": "操作成功",
    "data": null
  }
  ```
- **失败响应 (Error Responses)**:
  - `HTTP 409 Conflict` (用户名或邮箱已存在)（√）
    ```json
    {
      "code": 1,
      "message": "用户名已被占用",
      "data": null
    }
    ```
  - `HTTP 400 Bad Request` (输入不合法，如密码太短)（√）
    ```json
    {
      "code": 1,
      "message": "密码长度必须在8到20个字符之间",
      "data": null
    }
    ```

### 2.2 用户登录 (`POST /api/auth/login`)

- **功能**: 用户登录并获取JWT。

- **请求体 (Request Body)**: `application/json`
  ```json
  {
    "usernameOrEmail": "newUser",
    "password": "password123"
  }
  ```
  
- **成功响应 (Success Response)**: `HTTP 200 OK`
  ```json
  {
    "code": 0,
    "message": "操作成功",
    "data": "eyJhbGciOiJIUzI1NiJ9... (此处为生成的JWT字符串)"
  }
  ```
  
- **失败响应 (密码错误)**: `HTTP 401 Unauthorized`（√）
  
  ```json
  {
    "code": 1,
    "message": "密码错误",
    "data": null
  }
  ```
  
- **失败响应 (UserOrEmail不存在)**: `HTTP 400 Bad Request`(√)

  ```json
  {
      "code": 1,
      "message": "用户名/邮箱未注册，请注册后重试",
      "data": null
  }
  ```

  **失败响应 (用户名/邮箱、密码为空)**: `HTTP 400 Bad Request`(√)

  ```json
  {
      "code": 1,
      "message": "请输入用户名/邮箱",
      "data": null
  }{
      "code": 1,
      "message": "请输入密码",
      "data": null
  }
  ```

  

## 3. 后端组件设计

- **`entity.User`**: Java实体类，字段与数据库`user`表一一对应。
- **`config.EncoderConfig`**: 配置类，用于向Spring容器中提供一个`PasswordEncoder`的Bean（具体实现为`BCryptPasswordEncoder`）。
- **`mapper.UserMapper`**: MyBatis接口，提供`findByUsername`, `findByEmail`, `insert`等数据库原子操作方法。
- **`service.UserService`**: 业务逻辑核心。`register`方法将包含检查用户/邮箱是否存在、密码加密、调用mapper插入用户的逻辑。`login`方法将包含校验用户和密码、更新登录时间、生成JWT的逻辑。
- **`controller.UserController`**: API入口，接收HTTP请求，调用`UserService`处理业务，并根据处理结果返回统一的JSON响应。
- **`exception.GlobalExceptionHandler`**: 全局异常处理器。Service层抛出的特定异常（如`UserAlreadyExistsException`）将被此类捕获，并转换为对应的HTTP错误响应（如 409 Conflict）。
