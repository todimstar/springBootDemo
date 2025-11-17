# Categories 表索引分析

## 查询语句
```sql
SELECT * FROM categories WHERE is_active = TRUE ORDER BY sort_order DESC;
```

## 当前表结构
```sql
CREATE TABLE `categories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(200),
  `icon` VARCHAR(255),
  `post_count` INT DEFAULT 0,
  `sort_order` INT DEFAULT 0,
  `is_active` TINYINT(1) DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_sort_order` (`sort_order`)  -- 当前只有这一个索引
) ENGINE=InnoDB;
```

## 索引方案对比

### 方案1：只有 `idx_sort_order`（当前方案）

```sql
KEY `idx_sort_order` (`sort_order`)
```

**执行计划**：
```sql
EXPLAIN SELECT * FROM categories WHERE is_active = TRUE ORDER BY sort_order DESC;

-- 结果：
-- type: ALL (全表扫描)
-- Extra: Using where; Using filesort
```

**执行过程**：
1. 全表扫描，逐行判断 `is_active = TRUE`
2. 对结果进行排序（filesort）
3. 返回结果

**性能评估**：
- ✅ 数据量小（< 100条），性能完全够用
- ❌ 需要额外排序操作
- ⏱️ 查询时间：< 1ms

---

### 方案2：添加 `idx_is_active`

```sql
KEY `idx_sort_order` (`sort_order`),
KEY `idx_is_active` (`is_active`)  -- 新增
```

**执行计划**：
```sql
-- MySQL 可能选择使用 idx_is_active
-- type: ref
-- Extra: Using where; Using filesort
```

**执行过程**：
1. 使用 `idx_is_active` 索引找到 TRUE 的记录
2. 回表查询完整数据
3. 对结果进行排序（filesort）
4. 返回结果

**性能评估**：
- ⚠️ `is_active` 选择性太低（只有 TRUE/FALSE）
- ❌ 如果 90% 的记录都是 TRUE，索引几乎无效
- ❌ 仍需排序操作
- ⏱️ 查询时间：< 1ms（与方案1差不多）

**结论**：单独给 `is_active` 加索引**意义不大**！

---

### 方案3：复合索引 `idx_active_sort`（推荐）

```sql
-- 删除旧索引
DROP INDEX idx_sort_order ON categories;

-- 创建复合索引（顺序很重要！）
CREATE INDEX idx_active_sort ON categories (is_active, sort_order DESC);
```

**执行计划**：
```sql
EXPLAIN SELECT * FROM categories WHERE is_active = TRUE ORDER BY sort_order DESC;

-- 结果：
-- type: ref
-- key: idx_active_sort
-- Extra: Using index condition (不需要 filesort！)
```

**执行过程**：
1. 使用复合索引，直接定位到 `is_active = TRUE` 的区间
2. 索引内数据已按 `sort_order DESC` 排好序（关键优化！）
3. 顺序扫描索引，逐个回表查询完整数据
4. 返回结果（已有序，无需排序）

**性能评估**：
- ✅ 不需要 filesort（最大优化点）
- ✅ 覆盖所有查询条件
- ✅ 符合"最左前缀"原则
- ⏱️ 查询时间：< 1ms

**B+树结构**：
```
索引 idx_active_sort (is_active, sort_order DESC)

         根节点
         ↓
    ┌────────┐
    │is_active│
    └────────┘
      ↙     ↘
  FALSE     TRUE
            ↓
  ┌─────────────────┐
  │ sort_order DESC │  ← 这一层已经排好序了！
  ├─────────────────┤
  │ 100 → id:1     │
  │  90 → id:3     │
  │  80 → id:5     │
  │  70 → id:2     │
  │  60 → id:4     │
  └─────────────────┘
```

---

### 方案4：覆盖索引（理论最优，但不实用）

```sql
-- 将所有查询字段都放入索引（不推荐！）
CREATE INDEX idx_active_sort_cover ON categories
(is_active, sort_order DESC, name, description, icon, post_count, create_time);
```

**问题**：
- ❌ 索引过大，浪费存储空间
- ❌ 插入/更新性能下降
- ❌ 实际上 categories 表数据量小，回表代价可以接受

**结论**：不推荐

---

## 索引选择性分析

### 什么是索引选择性？
```
选择性 = DISTINCT(column) / COUNT(*)
```

选择性越高，索引效果越好：
- 选择性 = 1.0（如主键、唯一键）：最佳
- 选择性 > 0.2：适合建索引
- 选择性 < 0.2：索引效果差

### categories 表的选择性

```sql
-- 假设有 20 条分区数据
SELECT
    COUNT(DISTINCT id) / COUNT(*) AS id_selectivity,           -- 1.0
    COUNT(DISTINCT name) / COUNT(*) AS name_selectivity,       -- 1.0
    COUNT(DISTINCT sort_order) / COUNT(*) AS sort_selectivity, -- 1.0
    COUNT(DISTINCT is_active) / COUNT(*) AS active_selectivity -- 0.1
FROM categories;

-- 结果：
-- id_selectivity:     1.0   (20/20) ← 主键，完美
-- name_selectivity:   1.0   (20/20) ← 唯一键，完美
-- sort_selectivity:   1.0   (20/20) ← 适合建索引
-- active_selectivity: 0.1   (2/20)  ← 不适合单独建索引！
```

**结论**：`is_active` 单独建索引效果很差！

---

## 实际测试

### 插入测试数据

```sql
INSERT INTO categories (name, description, sort_order, is_active) VALUES
('技术分享', '编程技术、算法、架构设计等技术讨论', 100, TRUE),
('职场生活', '面试经验、职业规划、工作感悟', 90, TRUE),
('问答求助', '技术问题求助、代码debug', 80, TRUE),
('工具资源', '开发工具推荐、学习资源分享', 70, TRUE),
('闲聊灌水', '轻松话题、日常闲聊', 60, TRUE),
('已归档分区', '旧分区', 50, FALSE);
```

### 对比查询性能

```sql
-- 方案1：只有 idx_sort_order
EXPLAIN SELECT * FROM categories WHERE is_active = TRUE ORDER BY sort_order DESC;
-- type: ALL
-- rows: 6 (扫描所有行)
-- Extra: Using where; Using filesort

-- 方案3：复合索引 idx_active_sort
EXPLAIN SELECT * FROM categories WHERE is_active = TRUE ORDER BY sort_order DESC;
-- type: ref
-- rows: 5 (只扫描符合条件的行)
-- Extra: Using index condition (不需要 filesort！)
```

---

## 最左前缀原则

### 复合索引 `(is_active, sort_order)` 可以支持的查询

```sql
-- ✅ 使用索引（完整匹配）
WHERE is_active = TRUE ORDER BY sort_order DESC

-- ✅ 使用索引（只用第一列）
WHERE is_active = TRUE

-- ❌ 不使用索引（跳过第一列）
WHERE sort_order > 50  -- 无法使用复合索引！

-- ✅ 使用索引（WHERE + ORDER BY 都匹配）
WHERE is_active = TRUE ORDER BY sort_order ASC
```

**重要规则**：
- 复合索引 `(A, B, C)` 相当于创建了 `(A)`, `(A, B)`, `(A, B, C)` 三个索引
- 必须从最左边开始连续匹配
- 跳过左边的列，索引失效

---

## 推荐方案

### 对于 categories 表（数据量小）

**方案A：保持现状（推荐）**
```sql
-- 只保留 idx_sort_order
KEY `idx_sort_order` (`sort_order`)
```

**理由**：
- ✅ categories 表数据量很小（< 100条）
- ✅ 全表扫描也只需要 < 1ms
- ✅ 索引越少，插入/更新性能越好
- ✅ 维护成本低

**适用场景**：
- 分区数量 < 100 条
- 查询频率不是特别高（每秒 < 1000 次）

---

**方案B：复合索引（进阶优化）**
```sql
-- 如果追求极致性能
DROP INDEX idx_sort_order ON categories;
CREATE INDEX idx_active_sort ON categories (is_active, sort_order DESC);
```

**理由**：
- ✅ 消除 filesort 操作
- ✅ 对高频查询有帮助
- ✅ 符合生产环境最佳实践

**适用场景**：
- 首页每次都要查询分区列表
- 查询频率很高（每秒 > 1000 次）
- 追求极致性能

---

## 总结

| 方案 | 索引 | 是否 filesort | 性能 | 推荐度 |
|------|------|--------------|------|--------|
| 方案1 | `idx_sort_order` | ✅ 需要 | 很好（数据量小） | ⭐⭐⭐⭐ |
| 方案2 | `+ idx_is_active` | ✅ 需要 | 没提升 | ⭐ |
| 方案3 | `idx_active_sort` | ❌ 不需要 | 最佳 | ⭐⭐⭐⭐⭐ |
| 方案4 | 覆盖索引 | ❌ 不需要 | 过度优化 | ⭐⭐ |

**建议**：
- 现阶段：保持 `idx_sort_order`（方案1）
- 生产环境/高并发：改用 `idx_active_sort`（方案3）
- ❌ 不要单独给 `is_active` 加索引（方案2）

---

## 学习资源

1. **《高性能MySQL》第5章：创建高性能的索引**
2. **可视化B+树**：https://www.cs.usfca.edu/~galles/visualization/BPlusTree.html
3. **MySQL官方文档**：https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html
