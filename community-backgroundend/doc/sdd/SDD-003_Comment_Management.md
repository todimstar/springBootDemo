# 系统设计文档 (SDD) - 评论模块

**文档编号**: SDD-003
**模块名称**: Comment Management (评论管理)
**版本**: 1.0

## 1. 数据库设计

本模块需要一张新的数据表 `comment` 来存储评论信息。

**`comment` 表**

| 字段名        | 类型     | 约束/备注                                  |
| ------------- | -------- | ------------------------------------------ |
| `id`          | `BIGINT` | 主键, 自增                                 |
| `post_id`     | `BIGINT` | 非空, 外键, 关联 `posts.id`                |
| `user_id`     | `BIGINT` | 非空, 外键, 关联 `user.id`                 |
| `content`     | `TEXT`   | 非空, 评论内容                             |
| `create_time` | `DATETIME` | 创建时间, 默认 `CURRENT_TIMESTAMP`         |

**索引建议**:
- 在 `post_id` 字段上创建索引，以加速查询某篇帖子下的所有评论。

## 2. API 接口设计

评论是帖子的子资源，因此API设计应体现这种层级关系。

### 2.1 发表评论 (`POST /api/posts/{postId}/comments`)

- **功能**: 为一篇帖子创建新评论。
- **权限**: **需要认证**。
- **路径参数**: `{postId}` - 评论所属的帖子ID。
- **请求头**: `Authorization: Bearer <jwt>`
- **请求体**: `application/json`
  ```json
  {
    "content": "写得太好了，赞！"
  }
  ```
- **成功响应**: `HTTP 201 Created` - 返回创建的评论对象。(√)
- **失败响应**: `401 Unauthorized`, `400 Bad Request` (内容为空), `404 Not Found` (帖子ID不存在)。（√）

### 2.2 获取某篇帖子的评论列表 (`GET /api/posts/{postId}/comments`)

- **功能**: 获取一篇帖子下的所有评论。
- **权限**: **公开访问**。
- **路径参数**: `{postId}` - 帖子ID。
- **成功响应**: `HTTP 200 OK` - 返回评论对象数组。

### 2.3 删除评论 (`DELETE /api/comments/{commentId}`)

- **功能**: 删除单条评论。
- **权限**: **需要认证**，且必须是**评论作者本人**。
- **路径参数**: `{commentId}` - 要删除的评论的ID。
- **请求头**: `Authorization: Bearer <jwt>`
- **成功响应**: `HTTP 204 No Content`。
- **失败响应**: `401 Unauthorized`, `403 Forbidden` (不是作者), `404 Not Found` (评论ID不存在)。

## 3. 后端组件设计

- **`entity.Comment`**: 新的实体类，对应 `comment` 表。
- **`mapper.CommentMapper`**: 新的Mapper接口，提供 `insert`, `findByPostId`, `findById`, `deleteById` 等方法。
- **`service.CommentService`**: 新的Service接口及其实现。
  - `createComment(Long postId, Comment comment)`: 获取当前用户ID，设置 `postId` 和 `userId`，保存评论。
  - `getCommentsByPostId(Long postId)`: 调用Mapper查询。
  - `deleteComment(Long commentId)`: 实现所有权校验逻辑，然后删除。
- **`controller.CommentController`**: 新的Controller类，实现上述API。
