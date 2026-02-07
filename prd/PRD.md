# PRD：内容安全与审核流（Moderation Flow）

## Executive Summary
本 PRD 设计一套可解释、可复审、可追溯的内容审核体系，用于社区帖子/评论的合规与质量控制。  
方案以“规则引擎 + 风险评分 + 审核状态机”为核心，支持自动处置与人工复核闭环，并预留后续接入第三方内容安全/AI 模型的能力。

## Scope
**In Scope**
- 帖子/评论审核链路（自动判定 + 人工复审）
- 规则引擎、风险评分、审核队列、审计日志
- 管理员审核后台与规则管理

**Out of Scope（本期不做）**
- 申诉系统完整流程
- 多语言审核与多地域合规策略

## 用户与角色
- 游客：仅可浏览已发布内容  
- 注册用户：可发布/编辑/删除自己内容  
- 管理员：审核内容、管理规则、查看审计日志  

## Phase Plan
### Phase 1：Foundation（2 周内落地）
**Tasks:**
- [ ] 在 `community-backgroundend/sql/springboot_db.sql` 追加审核相关表结构  
- [ ] 实现规则扫描 + 风险评分服务（含可解释输出）  
- [ ] 实现管理员待审列表与审核决策接口  

### Phase 2：Operations（后续迭代）
**Tasks:**
- [ ] 规则管理后台（启停/版本/灰度）  
- [ ] 影子发布与抽样复核  
- [ ] 申诉流程与复审支持  

## Technical Stack
**Backend**
- Java 17  
- Spring Boot 3.3.5  
- Spring Security（随 Boot 版本）  
- MyBatis 3.0.3  
- MySQL 8.3.0（driver）  
- Redis（spring-boot-starter-data-redis）  
- JWT（jjwt 0.12.6）  
- MapStruct 1.5.5.Final  
- Knife4j 4.5.0  

**Frontend**
- Vue 3.5.18  
- Vue Router 4.5.1  
- Pinia 3.0.3  
- Axios 1.12.2  
- Vite 7.0.6  
- Element Plus 2.11.3  

## Features & Requirements（MoSCoW）
说明：标注“（新增）”的文件当前仓库不存在，为本期计划新增文件/目录。

### Feature 1：自动审核与风险评分
**MUST:** 自动审核与风险评分  
**Depends on:** 数据库结构变更 `community-backgroundend/sql/springboot_db.sql`（在该文件追加审核相关表）  
**Files:**  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/service/ModerationService.java`（新增）  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/service/impl/ModerationServiceImpl.java`（新增）  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/mapper/ModerationMapper.java`（新增）  
- `community-backgroundend/src/main/resources/mapper/ModerationMapper.xml`（新增）

**Acceptance Criteria:**
- [ ] 发帖/改帖进入审核链路，自动生成审核任务与风险评分  
- [ ] 评分可解释：返回命中规则清单与分值构成  
- [ ] 评分阈值触发“放行/送审/拦截”三类处置  

### Feature 2：内容状态机与可见性
**MUST:** 内容状态机与可见性  
**Depends on:** `community-backgroundend/sql/springboot_db.sql` 基础表结构  
**Files:**  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/POJO/entity/Post.java`  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/POJO/entity/Comment.java`  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/service/PostService.java`  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/service/CommentService.java`

**Acceptance Criteria:**
- [ ] Post/Comment 新增状态字段并驱动可见性（pending/rejected/removed/shadow）  
- [ ] 作者可见 shadow 内容，游客/普通用户不可见  
- [ ] 内容编辑后重新进入 PendingReview  

### Feature 3：审核队列与人工审核
**MUST:** 审核队列与人工审核  
**Depends on:** 管理员鉴权 `community-backgroundend/src/main/java/com/liu/springbootdemo/config/SecurityConfig.java`  
**Files:**  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/controller/admin/ModerationController.java`（新增）  
- `community-frontend/src/views/admin/ModerationQueueView.vue`（新增）  
- `community-frontend/src/router/index.js`

**Acceptance Criteria:**
- [ ] 管理员能分页查看待审列表并查看详情  
- [ ] 支持审批动作：通过/驳回/下架/影子发布  
- [ ] 审核动作必须填写理由并落库  

### Feature 4：审计日志与可追溯
**MUST:** 审计日志与可追溯  
**Depends on:** 审核任务表 `moderation_task`  
**Files:**  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/POJO/entity/ModerationDecision.java`（新增）  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/mapper/ModerationDecisionMapper.java`（新增）

**Acceptance Criteria:**
- [ ] 每次审核动作生成审计记录（时间/人/结果/理由）  
- [ ] 审计记录与审核任务强关联  
- [ ] 可按内容维度追溯完整审核链路  

### Feature 5：规则管理（启停/版本）
**SHOULD:** 规则管理（启停/版本）  
**Depends on:** 规则表 `moderation_rule`  
**Files:**  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/controller/admin/ModerationRuleController.java`（新增）  
- `community-frontend/src/views/admin/RuleManagerView.vue`（新增）

**Acceptance Criteria:**
- [ ] 规则支持新增/编辑/启停  
- [ ] 规则命中生效可通过版本号追踪  
- [ ] 规则启停不影响历史审核记录  

### Feature 6：影子发布
**SHOULD:** 影子发布  
**Depends on:** 内容状态机与权限校验  
**Files:**  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/service/PostService.java`  
- `community-frontend/src/views/PostDetailView.vue`

**Acceptance Criteria:**
- [ ] Shadow 内容仅作者可见  
- [ ] 管理端可切换 Shadow → Approved  

### Feature 7：失败降级与幂等
**SHOULD:** 失败降级与幂等  
**Depends on:** 全局异常处理 `community-backgroundend/src/main/java/com/liu/springbootdemo/common/exception/GlobalExceptionHandler.java`  
**Files:**  
- `community-backgroundend/src/main/java/com/liu/springbootdemo/service/impl/ModerationServiceImpl.java`（新增）

**Acceptance Criteria:**
- [ ] 审核服务异常时，内容进入 PendingReview  
- [ ] 相同内容 hash 重复审核不生成重复任务  

### Feature 8：申诉/抽样复核
**COULD:** 申诉/抽样复核  
**Depends on:** 审计日志与审核队列  
**Files:**  
- `community-frontend/src/views/admin/AppealReviewView.vue`（新增）

**Acceptance Criteria:**
- [ ] 支持标记内容为“待复核”  
- [ ] 复核记录可追溯  

## 主要接口（简版）
- `POST /api/posts` 创建内容 → 触发审核  
- `GET /api/admin/moderation/tasks?status=...` 审核列表  
- `GET /api/admin/moderation/tasks/{id}` 审核详情  
- `POST /api/admin/moderation/tasks/{id}/decision` 审核决策  
- `GET /api/admin/moderation/rules` 规则列表  
- `POST /api/admin/moderation/rules` 创建规则  
- `PATCH /api/admin/moderation/rules/{id}` 更新规则  
- `POST /api/admin/moderation/rules/{id}/enable` 启用  
- `POST /api/admin/moderation/rules/{id}/disable` 停用  

## Success Metrics
- 自动审核决策 p95 延迟 < 80ms  
- 发帖接口（自动放行）p95 < 300ms  
- 待审列表加载 < 500ms（20 条/页）  
- 审核决策落库成功率 ≥ 99.9%  
- 抽样误判率 < 3%（每周抽样 ≥ 200 条）  

## 非功能性要求
- 审核链路必须可解释（rule_hits 100% 可追溯）  
- 审核服务异常必须降级为 PendingReview  
 

---

## 表结构设计（MySQL）

### 1) 规则表：`moderation_rule`
```sql
CREATE TABLE moderation_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  type ENUM('keyword','regex','blacklist') NOT NULL,
  pattern VARCHAR(512) NOT NULL,
  score INT NOT NULL DEFAULT 0,
  action ENUM('pass','review','block') NOT NULL DEFAULT 'review',
  enabled TINYINT NOT NULL DEFAULT 1,
  version INT NOT NULL DEFAULT 1,
  updated_by BIGINT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_rule_type_enabled (type, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2) 审核任务表：`moderation_task`
```sql
CREATE TABLE moderation_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  content_type ENUM('post','comment') NOT NULL,
  content_id BIGINT NOT NULL,
  content_title VARCHAR(255) NULL,
  content_body TEXT NOT NULL,
  content_hash VARCHAR(64) NOT NULL,
  risk_score INT NOT NULL DEFAULT 0,
  status ENUM('pending','approved','rejected','removed','shadow') NOT NULL DEFAULT 'pending',
  rule_hits JSON NULL,
  reason VARCHAR(255) NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_task_status (status),
  INDEX idx_task_content (content_type, content_id),
  UNIQUE KEY uk_task_hash (content_hash, content_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3) 审核记录表：`moderation_decision`
```sql
CREATE TABLE moderation_decision (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  reviewer_id BIGINT NOT NULL,
  decision ENUM('approve','reject','remove','shadow') NOT NULL,
  reason VARCHAR(255) NOT NULL,
  decided_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (task_id) REFERENCES moderation_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 4) 用户风险画像表：`user_risk_profile`
```sql
CREATE TABLE user_risk_profile (
  user_id BIGINT PRIMARY KEY,
  violation_count INT NOT NULL DEFAULT 0,
  risk_score INT NOT NULL DEFAULT 0,
  last_violation_at DATETIME NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 5) 现有 `post` 表建议追加字段
```sql
ALTER TABLE post
  ADD COLUMN status ENUM('draft','pending','approved','rejected','removed','shadow') NOT NULL DEFAULT 'pending',
  ADD COLUMN risk_score INT NOT NULL DEFAULT 0,
  ADD COLUMN last_reviewed_at DATETIME NULL,
  ADD COLUMN last_review_id BIGINT NULL;
```

### 6) 现有 `comment` 表建议追加字段
```sql
ALTER TABLE comment
  ADD COLUMN status ENUM('pending','approved','rejected','removed','shadow') NOT NULL DEFAULT 'pending',
  ADD COLUMN risk_score INT NOT NULL DEFAULT 0,
  ADD COLUMN last_reviewed_at DATETIME NULL;
```
