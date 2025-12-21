# Posts表设计对比：外键 vs 无外键

## 方案A：有外键（传统企业级）

```sql
CREATE TABLE `posts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `category_id` BIGINT NULL DEFAULT NULL COMMENT '分区ID，草稿可为NULL',
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿,1待审核,2已发布,3已拒绝,4已删除',
  -- ... 其他字段
  PRIMARY KEY (`id`),
  INDEX `idx_category_id` (`category_id`),

  -- 外键约束：禁止删除有帖子的分区
  CONSTRAINT `fk_posts_categories`
    FOREIGN KEY (`category_id`)
    REFERENCES `categories` (`id`)
    ON DELETE RESTRICT
) ENGINE=InnoDB;
```

**特点**：
- ✅ 数据库层面强制数据完整性
- ✅ 多个应用访问时也能保证一致性
- ❌ 插入/删除性能下降（需要检查外键）
- ❌ 分库分表困难（外键跨库无法使用）
- ❌ 死锁风险增加

---

## 方案B：无外键（阿里规范/互联网公司）

```sql
CREATE TABLE `posts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `category_id` BIGINT NULL DEFAULT NULL COMMENT '分区ID，草稿可为NULL',
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿,1待审核,2已发布,3已拒绝,4已删除',
  -- ... 其他字段
  PRIMARY KEY (`id`),
  INDEX `idx_category_id` (`category_id`)

  -- 无外键约束，一切在代码层面控制
) ENGINE=InnoDB;
```

**特点**：
- ✅ 插入/删除性能更好
- ✅ 分库分表友好
- ✅ 应用层完全控制逻辑
- ❌ 依赖代码质量，可能出现脏数据
- ❌ 多应用访问时需要统一规范

---

## 业务逻辑代码（两种方案完全相同！）

### CategoryService.java

```java
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private PostMapper postMapper;

    /**
     * 删除分区
     * 注意：无论有没有外键，这段代码都应该写！
     */
    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        // 1. 检查分区是否存在
        Category category = categoryMapper.findById(categoryId);
        if (category == null) {
            throw new NotFindException("分区不存在");
        }

        // 2. 【关键】检查该分区下是否有帖子
        int postCount = postMapper.countByCategoryId(categoryId);
        if (postCount > 0) {
            throw new BizException("该分区下还有 " + postCount + " 个帖子，无法删除。请先迁移或删除帖子。");
        }

        // 3. 执行删除
        categoryMapper.deleteById(categoryId);

        // 有外键：如果上面漏了检查，这里数据库会抛异常（保底）
        // 无外键：完全依赖上面的业务检查
    }

    /**
     * 迁移分区下的所有帖子（管理员功能）
     */
    @Override
    @Transactional
    public void migratePosts(Long fromCategoryId, Long toCategoryId) {
        // 校验目标分区存在
        Category toCategory = categoryMapper.findById(toCategoryId);
        if (toCategory == null || !toCategory.getIsActive()) {
            throw new BizException("目标分区不存在或已禁用");
        }

        // 批量更新帖子的分区
        int updatedCount = postMapper.updateCategory(fromCategoryId, toCategoryId);

        // 更新分区的帖子计数
        categoryMapper.updatePostCount(toCategoryId, updatedCount);
        categoryMapper.updatePostCount(fromCategoryId, -updatedCount);
    }
}
```

### PostService.java

```java
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 发布帖子（从草稿发布或直接发布）
     */
    @Override
    @Transactional
    public Post publishPost(Post post) {
        // 1. 【关键】发布时必须选择分区
        if (post.getCategoryId() == null) {
            throw new InvalidInputException("发布帖子必须选择分区");
        }

        // 2. 校验分区是否存在且启用
        Category category = categoryMapper.findById(post.getCategoryId());
        if (category == null) {
            throw new BizException("所选分区不存在");
            // 有外键：插入时数据库也会报错
            // 无外键：只有这里检查
        }
        if (!category.getIsActive()) {
            throw new BizException("所选分区已被禁用");
        }

        // 3. 设置为已发布状态
        post.setStatus(2);

        // 4. 保存或更新
        if (post.getId() == null) {
            postMapper.insert(post);
        } else {
            postMapper.update(post);
        }

        // 5. 更新分区的帖子计数
        categoryMapper.incrementPostCount(post.getCategoryId());

        return post;
    }

    /**
     * 查询已发布的帖子列表
     * 注意：LEFT JOIN 分区表，过滤已禁用的分区
     */
    @Override
    public List<PostVO> listPublishedPosts(Long categoryId, Integer pageNum, Integer pageSize) {
        // 这个查询对有外键/无外键都一样
        return postMapper.findPublishedPostsWithCategory(categoryId, pageNum, pageSize);
    }
}
```

### PostMapper.java

```java
@Mapper
public interface PostMapper {

    /**
     * 统计某分区下的帖子数
     */
    @Select("SELECT COUNT(*) FROM posts WHERE category_id = #{categoryId}")
    int countByCategoryId(Long categoryId);

    /**
     * 批量更新帖子分区（迁移功能）
     */
    @Update("UPDATE posts SET category_id = #{toCategoryId} WHERE category_id = #{fromCategoryId}")
    int updateCategory(@Param("fromCategoryId") Long fromCategoryId,
                      @Param("toCategoryId") Long toCategoryId);

    /**
     * 查询已发布的帖子，LEFT JOIN 分区表
     * 关键：过滤分区被禁用的帖子
     */
    @Select("<script>" +
            "SELECT p.*, c.name as category_name, c.icon as category_icon " +
            "FROM posts p " +
            "LEFT JOIN categories c ON p.category_id = c.id " +
            "WHERE p.status = 2 " +
            "AND (c.id IS NULL OR c.is_active = TRUE) " +
            "<if test='categoryId != null'>" +
            "AND p.category_id = #{categoryId} " +
            "</if>" +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<PostVO> findPublishedPostsWithCategory(
            @Param("categoryId") Long categoryId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );
}
```

---

## 两种方案的实际差异

### 场景1：删除分区

**有外键**：
```java
categoryMapper.deleteById(5);
// 如果分区5下有帖子，数据库直接报错：
// Cannot delete or update a parent row: a foreign key constraint fails
```

**无外键**：
```java
categoryMapper.deleteById(5);
// 删除成功！但帖子的 category_id 仍然是 5（脏数据！）
// 所以必须在代码里先检查
```

### 场景2：插入帖子

**有外键**：
```java
post.setCategoryId(999); // 不存在的分区
postMapper.insert(post);
// 数据库报错：Cannot add or update a child row: a foreign key constraint fails
```

**无外键**：
```java
post.setCategoryId(999); // 不存在的分区
postMapper.insert(post);
// 插入成功！但是脏数据！
// 所以必须在代码里先校验分区存在
```

### 场景3：查询帖子

**有外键 和 无外键 完全相同**：
```sql
SELECT p.*, c.name as category_name
FROM posts p
LEFT JOIN categories c ON p.category_id = c.id
WHERE p.status = 2 AND c.is_active = TRUE;
```

---

## 推荐方案：无外键（阿里规范）

**原因**：
1. 你的项目目标是学习和面试
2. 互联网公司普遍不用外键
3. 性能更好，扩展性更强
4. 业务逻辑在代码层面更清晰可控

**注意事项**：
- ✅ 所有关联关系在Service层校验
- ✅ 查询时使用 LEFT JOIN 保证数据完整性
- ✅ 单元测试覆盖边界情况（分区不存在、分区被禁用等）
- ✅ 代码审查时重点检查关联数据的处理

---

## 修改建议

### 当前表结构检查

```sql
-- 查看 posts 表是否有外键
SHOW CREATE TABLE posts;

-- 如果有外键 fk_posts_categories，删除它
ALTER TABLE posts DROP FOREIGN KEY fk_posts_categories;
```

### 最终表结构

```sql
-- 确保 posts 表没有外键，category_id 允许 NULL
CREATE TABLE `posts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `category_id` BIGINT NULL DEFAULT NULL COMMENT '分区ID，草稿可为NULL',
  -- ... 其他字段
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB;
```

---

**总结**：无论选择哪种方案，业务代码应该保持一致。外键只是数据库层面的额外保护，不应替代业务逻辑校验。
