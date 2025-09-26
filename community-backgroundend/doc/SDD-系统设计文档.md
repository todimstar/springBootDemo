# 系统设计文档 (System Design Document)

## 1. 系统架构

本项目采用**前后端分离**架构。
- **后端**: 基于 Spring Boot 的单体应用，通过 RESTful API 提供服务。
- **前端**: (待定) 基于 Vue.js 的单页应用 (SPA)。
- **数据库**: 使用 MySQL 关系型数据库。

## 2. 数据库设计

### 2.1 实体关系图 (ER Diagram - 文字描述)
- 一个 `User` 可以拥有多个 `Post`。
- 一个 `User` 可以拥有多个 `Comment`。
- 一个 `Post` 可以拥有多个 `Comment`。

### 2.2 数据表结构

**用户表 (user)**
| 字段名        | 类型          | 约束/备注      |
|---------------|---------------|----------------|
| `id`          | BIGINT        | 主键, 自增     |
| `username`    | VARCHAR(50)   | 非空, 唯一     |
| `password`    | VARCHAR(255)  | 非空 (加密存储)|
| `email`       | VARCHAR(100)  | 唯一, 可为空   |
| `role`        | VARCHAR(50)   | 非空, 默认 'ROLE_USER' |
| `create_time` | DATETIME      | 创建时间, 默认当前时间 |
| `last_login_time` | DATETIME  | 上次登录时间, 可为空 |

**帖子表 (posts)**
| 字段名        | 类型          | 约束/备注      |
|---------------|---------------|----------------|
| `id`          | BIGINT        | 主键, 自增     |
| `title`       | VARCHAR(255)  | 非空           |
| `content`     | TEXT          | 非空, 帖子内容 |
| `user_id`     | BIGINT        | 非空, 外键, 关联user.id |
| `create_time` | DATETIME      | 创建时间, 默认当前时间 |
| `update_time` | DATETIME      | 更新时间, 默认当前时间且自动更新 |

**评论表 (comment)**
| 字段名        | 类型          | 约束/备注      |
|---------------|---------------|----------------|
| `id`          | BIGINT        | 主键, 自增     |
| `content`     | TEXT          | 非空, 评论内容 |
| `user_id`     | BIGINT        | 非空, 外键, 关联user.id |
| `post_id`     | BIGINT        | 非空, 外键, 关联posts.id |
| `create_time` | DATETIME      | 创建时间, 默认当前时间 |

## 3. API 端点设计 (V1)

### 3.1 用户认证
- `POST /api/auth/register`: 用户注册
- `POST /api/auth/login`: 用户登录

### 3.2 帖子
- `GET /api/posts`: 获取帖子列表（支持分页）
- `GET /api/posts/{id}`: 获取单个帖子详情
- `POST /api/posts`: 创建新帖子 (需认证)

### 3.3 评论
- `GET /api/posts/{postId}/comments`: 获取某篇帖子的评论列表
- `POST /api/posts/{postId}/comments`: 为某篇帖子创建新评论 (需认证)