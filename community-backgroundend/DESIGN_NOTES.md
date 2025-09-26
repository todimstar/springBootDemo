# 设计草图 (Design Notes)

本文档用于记录项目核心的设计决策，会随着开发过程不断演进。

## 1. 核心实体和关系

- **用户 (User)**: 拥有 `id`, `username`, `password` 等基本信息。
- **帖子 (Post)**: 拥有 `id`, `title`, `content` 等信息。

**核心关系**:
- 一个 `User` 可以发布多个 `Post` (一对多关系)。

## 2. 数据库表设计草案

- **user 表**:
  - `id` (主键)
  - `username` (唯一, 非空)
  - `password` (非空)
  - `create_time`

- **posts 表**:
  - `id` (主键)
  - `user_id` (外键, 关联 `user.id`)
  - `title` (非空)
  - `content` (非空)
  - `create_time`

## 3. API 接口设计草案 (V1)

- `POST /api/auth/register`: 处理用户注册请求。
- `POST /api/auth/login`: 处理用户登录请求。
- `POST /api/posts`: (需登录) 创建一个新帖子。
- `GET /api/posts`: 获取所有帖子的列表。
- `GET /api/posts/{id}`: 获取ID为{id}的帖子详情。
