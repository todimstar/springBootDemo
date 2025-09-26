# 系统设计文档 (SDD) - 帖子管理模块

**文档编号**: SDD-002
**模块名称**: Post Management (帖子管理)
**版本**: 1.1 (已更新)

## 1. 数据库设计

本模块将使用在 `SDD-001` 中已定义的 `posts` 表，无需新增表结构。

## 2. API 接口设计

### 2.1 创建帖子 (`POST /api/posts`)
- **功能**: 创建一个新帖子。
- **权限**: **需要认证**。
- **请求头**: `Authorization: Bearer <jwt>`
- **请求体**: `application/json` - `{"title": "...", "content": "..."}`
- **成功响应**: `HTTP 201 Created` - 返回创建的帖子对象。（√）
- **失败响应**: `401 Unauthorized`, `400 Bad Request`(空内容时)。（√）

### 2.2 获取帖子列表 (分页) (`GET /api/posts`)
- **功能**: 分页获取帖子列表。
- **权限**: **公开访问**。    （√）
- **查询参数**: `page` (页码), `size` (每页数量)。
- **校验规则 (Validation Rules) - [新增]**:（√）
  - 如果 `page` 参数 < 1, 服务端按 1 处理。
  - 如果 `size` 参数 < 1, 服务端按 10 处理。
  - `size` 参数最大值为 100，超过100按100处理。
- **成功响应**: `HTTP 200 OK` - 返回帖子对象数组。

### 2.3 更新帖子 (`PUT /api/posts/{id}`) - [新增]
- **功能**: 更新一篇帖子的标题和内容。
- **权限**: **需要认证**，且必须是**帖子作者本人**。（√）
- **路径参数**: `{id}` - 要更新的帖子的ID。
- **请求头**: `Authorization: Bearer <jwt>`
- **请求体**: `application/json` - `{"title": "...", "content": "..."}`
- **成功响应**: `HTTP 200 OK` - 返回更新后的帖子对象。（√）
- **失败响应**: 
  - `401 Unauthorized`: 未登录。（SecurityConfig的未登录，没错未登录就是没有jwt的。jwt过期也会返回401，走jwtFilter）-- （√）
  - `403 Forbidden`: 已登录但不是该帖子的作者。（√）
  - `404 Not Found`: 帖子ID不存在。（√）

### 2.4 删除帖子 (`DELETE /api/posts/{id}`) - [新增]
- **功能**: 删除一篇帖子。
- **权限**: **需要认证**，且必须是**帖子作者本人**。
- **路径参数**: `{id}` - 要删除的帖子的ID。
- **请求头**: `Authorization: Bearer <jwt>`
- **成功响应**: `HTTP 204 No Content` - 成功删除，响应体为空。（√）
- **失败响应**: 
  - `401 Unauthorized`: 未登录。（√，jwt和SecurityConfig会出手）
  - `403 Forbidden`: 已登录但不是该帖子的作者。（√）
  - `404 Not Found`: 帖子ID不存在。（√）

## 3. 后端组件设计

- **`mapper.PostMapper`**: 
  - 需要新增 `update(Post post)` 和 `deleteById(Long id)` 方法。
  - 需要 `findById(Long id)` 方法用于更新和删除前的查询。

- **`service.PostService`**: 
  - `createPost(Post post)`: 逻辑不变，但要从SecurityContext获取用户ID。
  - `getPostsByPage(...)`: 实现分页查询。
  - `updatePost(Long postId, Post postData)` - **[新增]**:
    1. 从 `SecurityContextHolder` 获取当前登录用户的ID。
    2. 调用 `PostMapper.findById(postId)` 从数据库中查出原始帖子。
    3. **核心校验**: 比较当前用户ID和原始帖子的 `userId` 是否一致。
    4. 如果不一致，抛出一个 `ForbiddenException` (自定义异常)。
    5. 如果一致，则更新帖子信息并保存。
  - `deletePost(Long postId)` - **[新增]**:
    1. 逻辑与 `updatePost` 类似，先查询，再进行所有权校验。
    2. 校验通过后，调用 `PostMapper.deleteById(postId)`。

- **`controller.PostController`**: 
  - 新增 `updatePost` 方法，映射到 `PUT /api/posts/{id}`。
  - 新增 `deletePost` 方法，映射到 `DELETE /api/posts/{id}`。

- **`exception.GlobalExceptionHandler`**:
  - 需要新增一个处理器来捕获 `ForbiddenException`，并将其转换为 `HTTP 403 Forbidden` 响应。

- **`config.SecurityConfig`**: 
  - 需确保 `PUT /api/posts/{id}` 和 `DELETE /api/posts/{id}` 都是 `authenticated()`。
