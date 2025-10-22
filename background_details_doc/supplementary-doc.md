# 项目文档补充与一致性审查报告

## 📋 对比说明

本文档整合了三份对话记录的内容：
- **当前版本**：2025-01-15 创建的完整文档体系
- **历史对话1**（ToClaude1）：2025-10-05 18:13
- **历史对话2**（ToClaude2）：2025-10-05 23:53

---

## ⚠️ 发现的不一致问题

### 1. 数据库表数量差异

| 来源 | 表数量 | 说明 |
|------|--------|------|
| 当前版本 | 13张 | 包含tags/post_tags（标注为可选）|
| 历史对话1 | 12张 | 完整的核心表 |
| 历史对话2 | 8张 | 只包含核心表，缺少reports等 |

**✅ 统一方案**：采用当前版本的13张表设计，原因：
- 功能更完整（包含举报、浏览历史）
- 预留了标签功能的扩展性
- 与产品需求文档一致

---

### 2. 点赞表设计差异

**历史对话1设计**（来源：ToClaude1）：
```sql
-- 分开设计
CREATE TABLE `post_likes` (
  post_id, user_id, create_time
)
CREATE TABLE `comment_likes` (
  comment_id, user_id, create_time
)
```

**历史对话2设计**（来源：ToClaude2）：
```sql
-- 统一设计
CREATE TABLE `user_likes` (
  user_id, target_type, target_id, create_time
)
-- target_type: 1-帖子 2-评论
```

**✅ 推荐方案**：采用**分开设计**（历史对话1）

**理由**：
- 查询性能更好（避免type字段判断）
- 索引更清晰（UNIQUE KEY直接约束）
- 符合单一职责原则
- 与当前数据库设计文档一致

**补充到**：数据库设计文档中添加设计决策说明

---

### 3. 开发周期差异

| 来源 | 周期 | 特点 |
|------|------|------|
| 当前版本 | 8-10周 | 详细到每天的任务 |
| 历史对话1 | 11周 | 包含前端联调和测试 |
| 历史对话2 | 5周 | 聚焦后端核心功能 |

**✅ 统一说明**：
- **5周版本**：纯后端开发，适合快速上手
- **8-10周版本**：后端完整开发，包含优化
- **11周版本**：包含前端对接和测试

**补充到**：开发计划文档中添加"灵活版本选择"章节

---

## 🆕 需要补充的核心内容

### 补充1：点赞功能完整实现代码（来源：历史对话1）

**重要性**：⭐⭐⭐⭐⭐ 这是面试必问的高频考点

```java
/**
 * 点赞系统完整实现 - Redis + DB 双写策略
 * 
 * 来源：历史对话1 (ToClaude1, 2025-10-05)
 * 技术要点：
 * 1. 防重复点赞（唯一索引 + Redis标记）
 * 2. 缓存一致性（Redis计数 + 定时同步DB）
 * 3. 分布式锁防并发
 */
@Slf4j
@Service
public class PostLikeService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private PostLikeMapper postLikeMapper;
    
    // Redis Key设计规范
    private static final String LIKE_KEY = "post:like:";           // 是否点赞
    private static final String COUNT_KEY = "post:like:count:";    // 点赞数
    private static final String USER_SET_KEY = "user:like:posts:"; // 用户点赞集合
    
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        String likeKey = LIKE_KEY + postId + ":" + userId;
        
        // 1. 检查Redis缓存（热数据）
        if (Boolean.TRUE.equals(redisTemplate.hasKey(likeKey))) {
            throw new BizException(ErrorCode.ALREADY_LIKED);
        }
        
        // 2. 检查数据库（防止缓存穿透）
        if (postLikeMapper.exists(postId, userId)) {
            // 补偿缓存
            redisTemplate.opsForValue().set(likeKey, "1", 7, TimeUnit.DAYS);
            throw new BizException(ErrorCode.ALREADY_LIKED);
        }
        
        try {
            // 3. 插入DB点赞记录
            postLikeMapper.insert(new PostLike(postId, userId));
            
            // 4. 更新Redis（三个操作保证一致性）
            redisTemplate.opsForValue().set(likeKey, "1", 7, TimeUnit.DAYS);
            
            String countKey = COUNT_KEY + postId;
            Long newCount = redisTemplate.opsForValue().increment(countKey);
            if (newCount == 1) {
                // 首次点赞，从DB读取真实值
                Integer dbCount = postMapper.getLikeCount(postId);
                redisTemplate.opsForValue().set(countKey, dbCount, 1, TimeUnit.DAYS);
            }
            
            redisTemplate.opsForSet().add(USER_SET_KEY + userId, postId);
            
            // 5. 更新DB计数（可选，也可定时任务同步）
            postMapper.incrementLikeCount(postId);
            
            log.info("点赞成功: postId={}, userId={}", postId, userId);
            
        } catch (Exception e) {
            // 回滚Redis
            redisTemplate.delete(likeKey);
            throw new BizException(ErrorCode.LIKE_FAILED);
        }
    }
    
    /**
     * 定时同步Redis点赞数到DB（保证最终一致性）
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时执行
    public void syncLikeCountToDB() {
        // 实现略
    }
}
```

**Mapper接口**：
```java
@Mapper
public interface PostLikeMapper {
    @Insert("INSERT INTO post_likes(post_id, user_id) VALUES(#{postId}, #{userId})")
    int insert(PostLike like);
    
    @Select("SELECT COUNT(*) > 0 FROM post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean exists(@Param("postId") Long postId, @Param("userId") Long userId);
}
```

**补充到**：创建新文档"核心功能实现代码"

---

### 补充2：缓存三大问题详解（来源：历史对话1）

**重要性**：⭐⭐⭐⭐⭐ 面试必问

#### 2.1 缓存穿透（查询不存在的数据）

**问题**：恶意请求大量不存在的数据，每次都打到DB
```java
// 问题示例
getPost(id=999999) → Redis没有 → DB没有 → 返回null
// 下次再查999999，还是走DB
```

**解决方案1：布隆过滤器**（来源：历史对话1）
```java
@Autowired
private BloomFilter<Long> postIdBloomFilter;

public Post getPost(Long postId) {
    // 先用布隆过滤器判断
    if (!postIdBloomFilter.mightContain(postId)) {
        return null; // 一定不存在
    }
    
    // 可能存在，继续查缓存和DB
    // ...
}

// 新增帖子时加入布隆过滤器
public void createPost(Post post) {
    postMapper.insert(post);
    postIdBloomFilter.put(post.getId());
}
```

**解决方案2：缓存空对象**（来源：历史对话1）
```java
public Post getPost(Long postId) {
    String key = "post:" + postId;
    
    // 1. 查Redis
    Post post = (Post) redisTemplate.opsForValue().get(key);
    if (post != null) {
        return post.getId() != null ? post : null; // 空对象返回null
    }
    
    // 2. 查DB
    post = postMapper.findById(postId);
    if (post == null) {
        // 缓存空对象（短时间）
        redisTemplate.opsForValue().set(key, new Post(), 5, TimeUnit.MINUTES);
        return null;
    }
    
    // 3. 正常数据缓存较长时间
    redisTemplate.opsForValue().set(key, post, 1, TimeUnit.HOURS);
    return post;
}
```

#### 2.2 缓存雪崩（大量缓存同时失效）

**问题**：所有热门帖子都设置1小时过期，同时失效导致DB瞬间压力暴增

**解决方案：过期时间加随机值**（来源：历史对话1）
```java
public void cachePost(Post post) {
    String key = "post:" + post.getId();
    
    // 1小时 + 随机0-10分钟
    int expireTime = 3600 + new Random().nextInt(600);
    redisTemplate.opsForValue().set(key, post, expireTime, TimeUnit.SECONDS);
}
```

#### 2.3 缓存击穿（热点数据失效）

**问题**：热门帖子缓存刚好失效，大量并发请求同时打到DB

**解决方案：分布式锁**（来源：历史对话1）
```java
public Post getPost(Long postId) {
    String cacheKey = "post:" + postId;
    
    // 1. 查缓存
    Post post = (Post) redisTemplate.opsForValue().get(cacheKey);
    if (post != null) return post;
    
    // 2. 缓存未命中，获取分布式锁
    String lockKey = "lock:post:" + postId;
    Boolean locked = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
    
    if (Boolean.TRUE.equals(locked)) {
        try {
            // 获取锁成功，查DB并更新缓存
            post = postMapper.findById(postId);
            if (post != null) {
                redisTemplate.opsForValue().set(cacheKey, post, 3600, TimeUnit.SECONDS);
            }
            return post;
        } finally {
            redisTemplate.delete(lockKey);
        }
    } else {
        // 获取锁失败，等待后重试
        Thread.sleep(100);
        return getPost(postId); // 递归重试
    }
}
```

**补充到**：技术选型文档的"Redis应用场景"章节

---

### 补充3：深分页优化方案对比（来源：历史对话1 + 历史对话2）

#### 方案对比表

| 方案 | 原理 | 优点 | 缺点 | 适用场景 |
|------|------|------|------|----------|
| **延迟关联** | 先查ID再关联 | 减少回表 | 仍需扫描大量行 | 中等数据量 |
| **游标分页** | 记录上次位置 | 性能最优 | 不能跳页 | ⭐推荐 |
| **Redis缓存** | 缓存ID列表 | 极快 | 内存消耗 | 热点数据 |
| **ES搜索** | 全文索引 | 功能强大 | 部署复杂 | 百万级以上 |

#### 方案1：延迟关联（来源：历史对话2）
```sql
-- MySQL优化器会先执行子查询
SELECT p.* FROM posts p
INNER JOIN (
    SELECT id FROM posts 
    ORDER BY create_time DESC 
    LIMIT 1000000, 20
) t ON p.id = t.id;
```

#### 方案2：游标分页（来源：当前版本 + 历史对话1）
```java
// Controller
@GetMapping
public Result getPosts(
    @RequestParam(required = false) Long lastId,  // 上一页最后一条ID
    @RequestParam(defaultValue = "20") int size
) {
    List<Post> posts;
    if (lastId == null) {
        // 第一页
        posts = postMapper.getFirstPage(size);
    } else {
        // 后续页
        posts = postMapper.getNextPage(lastId, size);
    }
    return Result.success(posts);
}

// Mapper
@Select("SELECT * FROM posts WHERE id < #{lastId} ORDER BY id DESC LIMIT #{size}")
List<Post> getNextPage(@Param("lastId") Long lastId, @Param("size") int size);
```

**优势**：
- 只需要索引扫描，不需要回表
- 性能稳定，不受数据量影响
- WHERE id < #{lastId} 直接利用主键索引

**补充到**：开发计划文档"第一阶段"的分页优化部分

---

### 补充4：评论楼中楼实现（来源：历史对话2）

**数据结构设计**：
```sql
ALTER TABLE comments ADD COLUMN (
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID',
    root_id BIGINT DEFAULT NULL COMMENT '根评论ID'
);

-- 示例数据
评论1 (id=1, parent_id=NULL, root_id=NULL)          -- 一级评论
  └─ 回复1-1 (id=2, parent_id=1, root_id=1)        -- 二级回复
  └─ 回复1-2 (id=3, parent_id=1, root_id=1)
      └─ 回复1-2-1 (id=4, parent_id=3, root_id=1) -- 三级回复
```

**查询SQL**（来源：历史对话2）：
```sql
-- 查询帖子的所有一级评论
SELECT c.*, u.username, u.avatar_url
FROM comments c
LEFT JOIN users u ON c.user_id = u.id
WHERE c.post_id = #{postId} AND c.parent_id IS NULL
ORDER BY c.create_time DESC
LIMIT #{offset}, #{size};

-- 查询某评论的所有子回复
SELECT c.*, u.username, u.avatar_url
FROM comments c
LEFT JOIN users u ON c.user_id = u.id
WHERE c.root_id = #{rootId}
ORDER BY c.create_time ASC;
```

**前端树形结构构建**（来源：历史对话2）：
```javascript
function buildCommentTree(comments) {
  const map = {};
  const roots = [];
  
  // 1. 构建Map
  comments.forEach(comment => {
    map[comment.id] = { ...comment, children: [] };
  });
  
  // 2. 建立父子关系
  comments.forEach(comment => {
    if (comment.parentId) {
      map[comment.parentId].children.push(map[comment.id]);
    } else {
      roots.push(map[comment.id]);
    }
  });
  
  return roots;
}
```

**补充到**：API接口文档的评论模块部分

---

### 补充5：WebSocket实时通知（来源：历史对话2）

**重要性**：⭐⭐⭐ 是项目亮点

**方案对比**（来源：历史对话2）：
| 方案 | 实现难度 | 实时性 | 资源消耗 | 推荐度 |
|------|---------|--------|----------|--------|
| 轮询 | ⭐ | ❌ 差 | 高 | ❌ |
| 长轮询 | ⭐⭐ | ⚠️ 一般 | 中 | ⚠️ |
| WebSocket | ⭐⭐⭐ | ✅ 优秀 | 低 | ✅ |
| SSE | ⭐⭐ | ✅ 良好 | 低 | ✅ |

**WebSocket实现**（来源：历史对话2）：
```java
@ServerEndpoint("/ws/notification/{userId}")
@Component
public class NotificationWebSocket {
    
    private static Map<Long, Session> sessions = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(@PathParam("userId") Long userId, Session session) {
        sessions.put(userId, session);
        log.info("用户{}建立WebSocket连接", userId);
    }
    
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        sessions.remove(userId);
        log.info("用户{}断开WebSocket连接", userId);
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        // 处理客户端消息（心跳等）
    }
    
    /**
     * 发送通知给指定用户
     */
    public static void sendToUser(Long userId, Notification notification) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(
                    JSON.toJSONString(notification)
                );
            } catch (IOException e) {
                log.error("发送通知失败", e);
            }
        }
    }
}
```

**前端连接**：
```javascript
// Vue3示例
const ws = new WebSocket(`ws://localhost:8080/ws/notification/${userId}`);

ws.onmessage = (event) => {
  const notification = JSON.parse(event.data);
  // 显示通知
  ElNotification({
    title: notification.title,
    message: notification.content,
    type: 'info'
  });
  // 更新未读数
  unreadCount.value++;
};
```

**补充到**：创建新文档"进阶功能实现"

---

### 补充6：面试高频Q&A（来源：历史对话1）

**整理成问答格式，便于背诵**

#### Q1: 为什么选择JWT而不是Session？（来源：历史对话1）

**标准答案**：
1. **无状态性**：JWT存储在客户端，服务器不需要存储会话信息，便于横向扩展
2. **跨域友好**：前后端分离架构下，JWT更适合跨域认证
3. **移动端友好**：APP开发中不依赖Cookie
4. **性能优势**：不需要每次请求都查Redis/数据库验证Session

**缺点及应对**：
- Token较长占带宽 → 压缩Token或使用短期Token
- 无法主动失效 → 维护黑名单或使用Refresh Token机制

#### Q2: 如何防止密码明文存储？（来源：历史对话1）

**答**：使用BCrypt加密
```java
// 注册时
String hashed = passwordEncoder.encode(rawPassword);

// 登录时
boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
```

**BCrypt优势**：
- 自动加盐，每次加密结果不同
- 慢哈希算法，防暴力破解
- 自适应，可调整计算强度

#### Q3: 点赞功能如何保证数据一致性？（来源：历史对话1）

**三层保障**：
1. **数据库层**：UNIQUE KEY防止重复插入
2. **应用层**：先查Redis/DB判断是否已点赞
3. **事务层**：@Transactional保证操作原子性

**高并发优化**：分布式锁
```java
String lockKey = "like:lock:" + postId + ":" + userId;
if (redisTemplate.setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS)) {
    try {
        // 点赞逻辑
    } finally {
        redisTemplate.delete(lockKey);
    }
}
```

**补充到**：创建新文档"面试准备手册"

---

## 🔍 一致性审查结果

### 1. 技术栈一致性 ✅

| 技术 | 当前版本 | 历史对话1 | 历史对话2 | 状态 |
|------|---------|-----------|-----------|------|
| Spring Boot | 3.x | 3.x | 3.x | ✅ |
| MyBatis | 3.5+ | 3.5+ | 3.5+ | ✅ |
| Redis | 7.x | 7.x | 未明确 | ✅ |
| MySQL | 8.0 | 8.0 | 8.0 | ✅ |
| JWT | 0.12+ | 0.11+ | 未明确 | ⚠️ 小差异 |

**修正**：统一使用JWT 0.12.x版本

---

### 2. API接口一致性检查

**当前版本接口数量**：44个
**历史对话中提及的重要接口**：

- ✅ POST /api/posts/{id}/like - 三份文档一致
- ✅ GET /api/notifications - 三份文档一致
- ⚠️ WebSocket /ws/notification/{userId} - 仅历史对话2提及

**补充**：将WebSocket接口添加到API文档

---

### 3. 数据库字段一致性

#### users表对比
| 字段 | 当前版本 | 历史对话1 | 历史对话2 | 状态 |
|------|---------|-----------|-----------|------|
| avatar_url | ✅ | ✅ | ✅ | ✅ |
| bio | ✅ | ✅ | ✅ | ✅ |
| nickname | ❌ | ✅ | ✅ | ⚠️ 缺失 |
| status | ✅ | ✅ | ✅ | ✅ |

**修正**：在users表补充nickname字段

```sql
ALTER TABLE users ADD COLUMN (
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称（可与username不同）'
);
```

#### posts表对比
| 字段 | 当前版本 | 历史对话1 | 历史对话2 | 状态 |
|------|---------|-----------|-----------|------|
| content_html | ❌ | ❌ | ✅ | ⚠️ 可选 |
| is_top | ❌ | ✅ | ❌ | ⚠️ 缺失 |

**修正**：补充is_top字段（置顶功能）

```sql
ALTER TABLE posts ADD COLUMN (
    is_top BOOLEAN DEFAULT FALSE COMMENT '是否置顶：0-否，1-是'
);
```

---

### 4. 开发流程一致性

**三份文档的开发重点对比**：

| 阶段 | 当前版本 | 历史对话1 | 历史对话2 |
|------|---------|-----------|-----------|
| Week1 | 数据库+参数校验 | 数据库重构 | 数据库+基础功能 |
| Week2 | 图片+互动 | 参数校验 | 点赞收藏关注 |
| Week3 | 通知+搜索 | 点赞收藏 | 图片+Markdown |
| Week4 | 管理后台 | 分类搜索 | 通知+管理 |
| Week5 | 优化 | 个人中心 | Redis优化 |

**✅ 结论**：三个版本的核心功能一致，只是顺序略有差异，都合理

---

## 📝 最终补充建议

### 立即补充的内容（高优先级）

1. **创建"核心功能实现代码"文档**
   - 点赞功能完整代码（来源：历史对话1）
   - 缓存三大问题解决方案（来源：历史对话1）
   - 评论楼中楼实现（来源：历史对话2）

2. **创建"面试准备手册"文档**
   - 高频Q&A问答（来源：历史对话1）
   - 项目介绍话术（来源：历史对话1）
   - 技术追问应对（来源：历史对话1）

3. **补充到数据库设计文档**
   - users表增加nickname字段
   - posts表增加is_top字段
   - 点赞表设计决策说明

4. **补充到技术选型文档**
   - WebSocket实时通知方案（来源：历史对话2）
   - 深分页优化方案对比表（来源：历史对话1+2）

### 可选补充（中等优先级）

5. **创建"进阶功能实现"文档**
   - WebSocket详细代码（来源：历史对话2）
   - 布隆过滤器应用（来源：历史对话1）
   - ES全文搜索集成（可选）

---

## ✅ 审查结论

### 优点
1. ✅ 三份文档的核心技术栈完全一致
2. ✅ 数据库设计理念统一（分层清晰）
3. ✅ 当前版本的文档最完整（有原型页面）

### 需要修正
1. ⚠️ 补充nickname和is_top字段
2. ⚠️ 统一JWT版本号
3. ⚠️ 补充WebSocket接口文档

### 建议
1. 历史对话1的**点赞实现代码**非常详细，必须补充
2. 历史对话1的**面试Q&A**格式很好，直接可用
3. 历史对话2的**楼中楼实现**代码完整，可直接使用

---

**审查日期**：2025-01-15  
**审查人**：Claude  
**结论**：✅ 通过审查，建议按上述方案补充

