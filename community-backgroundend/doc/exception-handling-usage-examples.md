# 异常处理重构 - 实际使用示例

## Service层改造示例

### 1. UserServiceImpl 改造示例

```java
package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.common.utils.Assert;
import com.liu.springbootdemo.entity.User;
import com.liu.springbootdemo.mapper.UserMapper;
import com.liu.springbootdemo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserServiceImplRefactored implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册 - 使用新的异常处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(User user) {
        // 使用断言工具类进行参数校验
        Assert.hasText(user.getUsername(), ErrorCode.PARAM_MISSING, "用户名不能为空");
        Assert.hasText(user.getEmail(), ErrorCode.PARAM_MISSING, "邮箱不能为空");
        Assert.hasText(user.getPassword(), ErrorCode.PARAM_MISSING, "密码不能为空");

        // 验证邮箱格式
        Assert.isEmail(user.getEmail(), ErrorCode.EMAIL_INVALID);

        // 验证密码长度
        Assert.lengthBetween(user.getPassword(), 6, 20, ErrorCode.PASSWORD_TOO_SHORT);

        // 检查用户名是否存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            // 方式1：使用默认消息
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);

            // 方式2：自定义消息
            // throw new BusinessException(ErrorCode.USERNAME_EXISTS,
            //         String.format("用户名 '%s' 已被占用", user.getUsername()));

            // 方式3：使用静态工厂方法
            // throw BusinessException.of(ErrorCode.USERNAME_EXISTS, user.getUsername());
        }

        // 检查邮箱是否存在
        User existingEmail = userMapper.findByEmail(user.getEmail());
        if (existingEmail != null) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        // 加密密码并保存
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setCreatedAt(LocalDateTime.now());

        int result = userMapper.insert(user);
        if (result <= 0) {
            log.error("用户注册失败，数据库插入异常: {}", user.getUsername());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，请稍后重试");
        }

        log.info("用户注册成功: {}", user.getUsername());
        return user;
    }

    /**
     * 用户登录 - 使用新的异常处理
     */
    @Override
    public LoginResponseDTO login(String usernameOrEmail, String password) {
        // 参数校验
        Assert.hasText(usernameOrEmail, ErrorCode.PARAM_MISSING, "请输入用户名或邮箱");
        Assert.hasText(password, ErrorCode.PARAM_MISSING, "请输入密码");

        // 查找用户
        User user = userMapper.findByUsernameOrEmail(usernameOrEmail);

        // 使用断言简化判断
        Assert.notNull(user, ErrorCode.ACCOUNT_NOT_FOUND, "账号不存在");

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("登录失败 - 密码错误, 用户: {}", usernameOrEmail);
            // 为了安全，不区分账号不存在和密码错误
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        // 检查用户状态
        if ("BANNED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        // 生成token
        String token = JwtUtil.generateToken(user.getUsername(), user.getRole());

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateLastLogin(user.getId(), user.getLastLoginAt());

        log.info("用户登录成功: {}", user.getUsername());

        return LoginResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * 获取用户信息
     */
    @Override
    public User getUserById(Long userId) {
        Assert.notNull(userId, ErrorCode.PARAM_MISSING);
        Assert.greaterThan(userId, 0, ErrorCode.PARAM_OUT_OF_RANGE);

        User user = userMapper.findById(userId);

        // 方式1：使用断言
        Assert.notNull(user, ErrorCode.USER_NOT_FOUND);

        // 方式2：手动判断（如果需要记录日志或其他操作）
        // if (user == null) {
        //     log.warn("用户不存在: userId={}", userId);
        //     throw new BusinessException(ErrorCode.USER_NOT_FOUND,
        //             String.format("用户ID %d 不存在", userId));
        // }

        return user;
    }
}
```

### 2. PostServiceImpl 改造示例

```java
package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.common.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PostServiceImplRefactored implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 创建帖子
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Post createPost(Post post) {
        // 参数校验
        Assert.hasText(post.getTitle(), ErrorCode.POST_TITLE_EMPTY);
        Assert.hasText(post.getContent(), ErrorCode.POST_CONTENT_EMPTY);
        Assert.lengthBetween(post.getTitle(), 1, 200, ErrorCode.POST_TITLE_TOO_LONG);

        // 获取当前用户
        User currentUser = securityUtil.getCurrentUser();
        Assert.notNull(currentUser, ErrorCode.UNAUTHORIZED);

        // 设置帖子信息
        post.setUserId(currentUser.getId());
        post.setCreatedAt(LocalDateTime.now());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);

        try {
            int result = postMapper.insert(post);
            if (result <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建帖子失败");
            }

            log.info("帖子创建成功: id={}, title={}, userId={}",
                    post.getId(), post.getTitle(), currentUser.getId());

            return post;

        } catch (DataIntegrityViolationException e) {
            // 处理数据库约束冲突
            log.error("创建帖子失败，数据库约束冲突", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建帖子失败", e);
        }
    }

    /**
     * 更新帖子
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Post updatePost(Long postId, Post updateData) {
        Assert.notNull(postId, ErrorCode.PARAM_MISSING);

        // 查找帖子
        Post existingPost = postMapper.findById(postId);
        if (existingPost == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 检查是否已删除
        if (existingPost.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.POST_ALREADY_DELETED);
        }

        // 检查权限
        User currentUser = securityUtil.getCurrentUser();
        if (!existingPost.getUserId().equals(currentUser.getId())) {
            // 附加详细信息用于调试
            Map<String, Object> details = new HashMap<>();
            details.put("postId", postId);
            details.put("postAuthorId", existingPost.getUserId());
            details.put("currentUserId", currentUser.getId());

            throw new BusinessException(
                ErrorCode.POST_NOT_AUTHOR,
                String.format("您不是帖子 '%s' 的作者", existingPost.getTitle()),
                details
            );
        }

        // 更新帖子
        if (updateData.getTitle() != null) {
            Assert.hasText(updateData.getTitle(), ErrorCode.POST_TITLE_EMPTY);
            existingPost.setTitle(updateData.getTitle());
        }

        if (updateData.getContent() != null) {
            Assert.hasText(updateData.getContent(), ErrorCode.POST_CONTENT_EMPTY);
            existingPost.setContent(updateData.getContent());
        }

        existingPost.setUpdatedAt(LocalDateTime.now());

        int result = postMapper.update(existingPost);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新帖子失败");
        }

        log.info("帖子更新成功: id={}, userId={}", postId, currentUser.getId());
        return existingPost;
    }

    /**
     * 删除帖子（软删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId) {
        Assert.notNull(postId, ErrorCode.PARAM_MISSING);

        Post post = postMapper.findById(postId);
        Assert.notNull(post, ErrorCode.POST_NOT_FOUND);

        // 检查是否已删除
        if (post.getDeletedAt() != null) {
            log.warn("尝试删除已删除的帖子: postId={}", postId);
            throw new BusinessException(ErrorCode.POST_ALREADY_DELETED);
        }

        // 检查权限
        User currentUser = securityUtil.getCurrentUser();
        Assert.isTrue(
            post.getUserId().equals(currentUser.getId()),
            ErrorCode.POST_NOT_AUTHOR,
            "您无权删除此帖子"
        );

        // 软删除
        post.setDeletedAt(LocalDateTime.now());
        int result = postMapper.softDelete(postId);

        if (result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除帖子失败");
        }

        log.info("帖子删除成功: id={}, userId={}", postId, currentUser.getId());
    }

    /**
     * 获取帖子详情
     */
    @Override
    public PostDetailVO getPostDetail(Long postId) {
        Assert.notNull(postId, ErrorCode.PARAM_MISSING);

        Post post = postMapper.findById(postId);

        // 如果帖子不存在，返回友好的错误信息
        if (post == null) {
            throw BusinessException.format(
                ErrorCode.POST_NOT_FOUND,
                "帖子 #%d 不存在或已被删除",
                postId
            );
        }

        // 检查帖子是否被锁定（假设有这个功能）
        if ("LOCKED".equals(post.getStatus())) {
            throw new BusinessException(ErrorCode.POST_LOCKED, "该帖子已被管理员锁定");
        }

        // 增加浏览量
        postMapper.incrementViewCount(postId);

        // 构建返回对象
        return PostDetailVO.from(post);
    }
}
```

### 3. CategoryServiceImpl 改造示例

```java
package com.liu.springbootdemo.service.impl;

@Slf4j
@Service
public class CategoryServiceImplRefactored implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private PostMapper postMapper;

    /**
     * 创建分区
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category createCategory(CategoryDTO dto) {
        // 参数校验
        Assert.hasText(dto.getName(), ErrorCode.PARAM_MISSING, "分区名称不能为空");
        Assert.lengthBetween(dto.getName(), 1, 50, ErrorCode.PARAM_OUT_OF_RANGE);

        // 检查名称是否重复
        Category existing = categoryMapper.findByName(dto.getName());
        if (existing != null) {
            throw new BusinessException(
                ErrorCode.CATEGORY_NAME_EXISTS,
                String.format("分区名称 '%s' 已存在", dto.getName())
            );
        }

        // 创建分区
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setPostCount(0);
        category.setCreatedAt(LocalDateTime.now());

        int result = categoryMapper.insert(category);
        if (result <= 0) {
            log.error("创建分区失败: name={}", dto.getName());
            throw new BusinessException(ErrorCode.CATEGORY_CREATE_FAILED);
        }

        log.info("分区创建成功: id={}, name={}", category.getId(), category.getName());
        return category;
    }

    /**
     * 删除分区
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        Assert.notNull(categoryId, ErrorCode.PARAM_MISSING);

        // 查找分区
        Category category = categoryMapper.findById(categoryId);
        if (category == null) {
            throw BusinessException.format(
                ErrorCode.CATEGORY_NOT_FOUND,
                "分区 #%d 不存在",
                categoryId
            );
        }

        // 检查是否有帖子
        int postCount = postMapper.countByCategoryId(categoryId);
        if (postCount > 0) {
            // 返回详细信息
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

        // 删除分区
        int result = categoryMapper.deleteById(categoryId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.CATEGORY_DELETE_FAILED);
        }

        log.info("分区删除成功: id={}, name={}", categoryId, category.getName());
    }

    /**
     * 批量操作示例 - 展示事务回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateCategories(List<CategoryDTO> categories) {
        Assert.notEmpty(categories, ErrorCode.PARAM_MISSING);

        for (int i = 0; i < categories.size(); i++) {
            CategoryDTO dto = categories.get(i);
            try {
                createCategory(dto);
            } catch (BusinessException e) {
                // 记录失败的位置和原因
                log.error("批量创建分区失败，第 {} 个分区: {}", i + 1, dto.getName(), e);

                // 抛出带上下文的异常
                Map<String, Object> context = new HashMap<>();
                context.put("failedIndex", i);
                context.put("failedCategory", dto.getName());
                context.put("originalError", e.getMessage());

                throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    String.format("批量创建失败，在第 %d 个分区 '%s' 处出错",
                        i + 1, dto.getName()),
                    context
                );
            }
        }
    }
}
```

## Controller层改造示例

```java
package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.POJO.vo.Result;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.utils.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@Tag(name = "帖子管理", description = "帖子相关接口")
@Validated  // 启用方法级参数校验
public class PostControllerRefactored {

    @Autowired
    private PostService postService;

    /**
     * 创建帖子
     *
     * 注意：Controller层不要捕获异常，让全局异常处理器处理
     */
    @PostMapping
    @Operation(summary = "创建帖子")
    public Result<Post> createPost(@Valid @RequestBody PostCreateDTO dto) {
        // Controller层只做简单的参数转换，不做业务校验
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategoryId(dto.getCategoryId());

        // 业务逻辑和异常都在Service层处理
        Post createdPost = postService.createPost(post);
        return Result.success(createdPost);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取帖子详情")
    public Result<PostDetailVO> getPost(
            @PathVariable("id") @Min(value = 1, message = "帖子ID必须大于0") Long postId) {
        // 路径参数校验由 @Min 注解处理
        PostDetailVO detail = postService.getPostDetail(postId);
        return Result.success(detail);
    }

    /**
     * 更新帖子
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新帖子")
    public Result<Post> updatePost(
            @PathVariable("id") Long postId,
            @Valid @RequestBody PostUpdateDTO dto) {

        Post updateData = new Post();
        updateData.setTitle(dto.getTitle());
        updateData.setContent(dto.getContent());

        Post updatedPost = postService.updatePost(postId, updateData);
        return Result.success(updatedPost);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除帖子")
    public Result<Void> deletePost(@PathVariable("id") Long postId) {
        postService.deletePost(postId);
        return Result.success();
    }

    /**
     * 分页查询帖子
     */
    @GetMapping
    @Operation(summary = "分页查询帖子")
    public Result<PageResult<Post>> listPosts(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {

        // 参数校验
        Assert.between(size, 1, 100, ErrorCode.PARAM_OUT_OF_RANGE);

        PageRequest request = PageRequest.builder()
                .page(page)
                .size(size)
                .categoryId(categoryId)
                .keyword(keyword)
                .build();

        PageResult<Post> result = postService.listPosts(request);
        return Result.success(result);
    }
}
```

## 测试示例

```java
@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreatePost_TitleEmpty_ShouldReturn400() throws Exception {
        String json = "{\"content\":\"test content\"}";

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + getValidToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("40011"))  // POST_TITLE_EMPTY
                .andExpect(jsonPath("$.message").value("帖子标题不能为空"));
    }

    @Test
    void testGetPost_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("40001"))  // POST_NOT_FOUND
                .andExpect(jsonPath("$.data.path").value("/api/posts/999999"));
    }

    @Test
    void testDeletePost_NotAuthor_ShouldReturn403() throws Exception {
        // 使用另一个用户的token尝试删除帖子
        mockMvc.perform(delete("/api/posts/1")
                .header("Authorization", "Bearer " + getOtherUserToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("40002"))  // POST_NOT_AUTHOR
                .andExpect(jsonPath("$.message").exists());
    }
}
```

## 迁移步骤

1. **第一阶段：添加新的异常体系**
   - 创建 ErrorCode 枚举
   - 创建 BusinessException 类
   - 创建 Assert 工具类
   - 创建新的 GlobalExceptionHandler（保留对旧异常的兼容）

2. **第二阶段：逐步迁移Service**
   - 先迁移新功能的Service
   - 逐个迁移现有Service，一个方法一个方法地改
   - 保持测试通过

3. **第三阶段：迁移Controller**
   - 移除Controller中的try-catch
   - 使用 @Valid 进行参数校验
   - 让异常向上传递到全局处理器

4. **第四阶段：清理旧代码**
   - 确认所有旧异常都已迁移
   - 删除旧的异常类
   - 删除旧的异常处理代码

## 最佳实践总结

1. **Controller层**：只做参数接收和转换，不处理异常
2. **Service层**：进行业务校验和抛出业务异常
3. **Mapper层**：只负责数据访问，不处理业务逻辑
4. **全局异常处理器**：统一处理所有异常，返回标准格式

5. **使用Assert工具类**：简化校验代码，提高可读性
6. **记录日志**：异常时记录详细日志，包括请求ID便于追踪
7. **返回友好消息**：给用户返回友好的错误提示，不暴露内部细节
8. **开发环境调试**：开发环境可返回详细错误信息，生产环境隐藏