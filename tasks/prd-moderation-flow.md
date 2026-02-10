# PRD: 内容安全与审核流（Moderation Flow）

## 1. Introduction / 概述

为社区帖子与评论建立**可解释、可复审、可追溯**的内容审核体系。内容在发布时经过同步规则引擎快速判定——自动放行、自动拦截或影子发布；命中灰区的内容异步进入人工审核队列。所有审核动作均留存审计日志，支持运营治理与合规追溯。

当前痛点：
- 内容发布无审核闭环，违规内容可直接传播
- 误判无法追溯，缺乏申诉与复审依据
- 管理效率低，全量人工审核不可扩展

## 2. Goals / 目标

- 对帖子（创建 & 编辑）和评论的文本内容进行实时风险评分
- 结合用户画像（注册时长、历史违规次数等）对评分加权
- 按阈值自动放行 / 影子发布 / 送人工审核 / 自动拦截，减少人工审核量 ≥ 60%
- 提供简易管理后台，支持审核队列浏览与通过/驳回/下架/影子操作
- 审计日志完整记录命中规则、评分、审核人、操作时间与理由
- 图片审核预留扩展接口，第一期不实现图片检测逻辑

## 3. User Stories / 用户故事

### US-001: 内容风险评分引擎
**Description:** 作为系统，我需要在帖子/评论提交时对文本进行规则匹配与风险评分，以便自动判定内容处置方式。

**Acceptance Criteria:**
- [ ] 规则引擎接收文本内容，依次执行：关键词匹配 → 正则匹配 → 黑名单检查
- [ ] 文本预处理：去空格、大小写统一、全半角归一化后再匹配
- [ ] 每条命中规则返回规则 ID、规则类型、命中片段、该规则权重分
- [ ] 汇总所有命中规则的加权分，输出最终风险分（0-100）
- [ ] 规则库数据存储在数据库，支持动态增删改（无需重启）
- [ ] 单次评分耗时 < 200ms（1000 字以内文本）
- [ ] 单元测试覆盖：无命中、单规则命中、多规则叠加、边界值场景
- [ ] Typecheck / 编译通过

### US-002: 用户画像风险加权
**Description:** 作为系统，我需要根据用户画像因子对风险评分进行加权，使新用户或有违规历史的用户受到更严格的审核。

**Acceptance Criteria:**
- [ ] 画像因子至少包含：注册天数、历史违规次数、账号等级
- [ ] 定义加权系数映射表，初始经验值：注册 < 7 天 → ×1.5，违规 ≥ 3 次 → ×2.0
- [ ] 加权系数可通过数据库配置，无需硬编码
- [ ] 最终评分 = 文本基础分 × 用户画像加权系数，上限封顶 100
- [ ] 单元测试覆盖各画像因子的独立与组合场景
- [ ] Typecheck / 编译通过

### US-003: 审核决策与状态流转
**Description:** 作为系统，我需要根据风险评分阈值自动决策内容状态，灰区内容进入人工审核队列。

**Acceptance Criteria:**
- [ ] **帖子状态复用现有 `Post.status` 字段**，在现有值基础上新增 `5 = SHADOW_BANNED`（影子发布）。完整映射：0=draft, 1=pending_review, 2=published, 3=rejected, 4=deleted, **5=shadow_banned**
- [ ] **评论表新增 `status` 字段**（int），值映射：0=pending_review, 1=approved, 2=rejected, 3=shadow_banned, 4=taken_down。默认 `0`（待审核）
- [ ] 审核决策到帖子状态的映射：自动放行 → status=2(published)，送人审 → status=1(pending_review)，自动拦截 → status=3(rejected)，影子发布 → status=5(shadow_banned)
- [ ] 阈值配置存储在数据库：如 score < 30 → 自动放行，30 ≤ score < 70 → 送人审，score ≥ 70 → 自动拦截
- [ ] 阈值可在管理后台动态调整
- [ ] 同步链路：规则引擎判定 → 自动放行或拦截的内容立即更新状态并返回
- [ ] 异步链路：灰区内容状态为 `PENDING_REVIEW`，写入审核队列表
- [ ] 单元测试覆盖每个阈值区间的决策路径
- [ ] Typecheck / 编译通过

### US-004: 影子发布可见性控制
**Description:** 作为用户，当我的内容被影子发布时，我能正常看到自己的帖子/评论，但其他用户不可见。

**Acceptance Criteria:**
- [ ] 状态为 `SHADOW_BANNED`（Post.status=5）的内容：发布者本人查询时正常返回
- [ ] 其他用户查询帖子列表/评论列表时，过滤掉 `SHADOW_BANNED` 状态的内容
- [ ] 发布者无任何提示表明内容被影子处理（无 UI 标记）
- [ ] 集成测试覆盖：发布者视角 vs 其他用户视角
- [ ] Typecheck / 编译通过

### US-005: 管理员审核队列页面
**Description:** 作为管理员，我需要一个简易审核页面，查看待审核内容列表并执行审核操作。

**Acceptance Criteria:**
- [ ] 审核列表页展示：内容摘要、作者、发布时间、风险分、命中规则摘要
- [ ] 支持按风险分降序排列（高风险优先）
- [ ] 支持按内容类型（帖子/评论）筛选
- [ ] 每条记录提供操作按钮：通过 / 驳回 / 下架 / 影子发布
- [ ] 操作时必须填写审核理由（文本输入，不可为空）
- [ ] 操作成功后列表实时刷新，已处理项移出队列
- [ ] Typecheck / 编译通过
- [ ] 在浏览器中验证页面交互（使用 dev-browser skill）

### US-006: 审核操作 API
**Description:** 作为开发者，我需要提供审核操作的后端 API，供管理后台页面调用。

**Acceptance Criteria:**
- [ ] `GET /api/admin/moderation/queue` — 分页查询待审核列表，支持按类型/风险分筛选与排序
- [ ] `GET /api/admin/moderation/{id}` — 查看审核详情（含完整命中规则明细）
- [ ] `POST /api/admin/moderation/{id}/action` — 执行审核操作（action: approve/reject/takedown/shadow，reason: 必填）
- [ ] 接口鉴权：仅 ADMIN 角色可访问
- [ ] 参数校验：非法 action、空 reason 返回 400
- [ ] 操作幂等：重复提交同一操作不报错，返回当前状态
- [ ] 单元测试 + 接口测试覆盖正常流与异常流
- [ ] Typecheck / 编译通过

### US-007: 审计日志记录与查询
**Description:** 作为管理员，我需要查看每条内容的完整审核轨迹，以便追溯和复审。

**Acceptance Criteria:**
- [ ] 审计日志表记录：目标 ID、目标类型、操作类型、操作人、操作时间、审核理由、命中规则快照、风险评分快照
- [ ] 自动审核（系统判定）同样写入日志，操作人标记为 `SYSTEM`
- [ ] `GET /api/admin/moderation/{id}/logs` — 按时间倒序返回该内容的审计轨迹
- [ ] 日志不可篡改（仅 INSERT，无 UPDATE/DELETE）
- [ ] 单元测试覆盖自动审核与人工审核的日志写入
- [ ] Typecheck / 编译通过

### US-008: 规则库管理 API
**Description:** 作为管理员，我需要通过 API 管理审核规则（增删改查），以便灵活应对新型违规内容。

**Acceptance Criteria:**
- [ ] `GET /api/admin/moderation/rules` — 分页查询规则列表
- [ ] `POST /api/admin/moderation/rules` — 新增规则（类型：keyword/regex/blacklist，内容，权重分，启用状态）
- [ ] `PUT /api/admin/moderation/rules/{id}` — 修改规则
- [ ] `DELETE /api/admin/moderation/rules/{id}` — 软删除规则
- [ ] 规则变更写入审计日志
- [ ] 规则加载支持缓存，变更后自动刷新缓存
- [ ] 接口鉴权：仅 ADMIN 角色
- [ ] 单元测试 + 接口测试
- [ ] Typecheck / 编译通过

### US-009: 图片审核扩展接口占位
**Description:** 作为开发者，我需要预留图片审核的扩展点，以便后续接入第三方图片检测服务。

**Acceptance Criteria:**
- [ ] 定义 `ContentModerationStrategy` 接口，包含 `evaluate(Content content): ModerationResult` 方法
- [ ] 文本审核实现该接口（`TextModerationStrategy`）
- [ ] 预留 `ImageModerationStrategy` 空实现（直接返回通过，风险分 0）
- [ ] 审核引擎通过策略模式按内容类型分发
- [ ] 后续接入图片审核只需实现该接口，无需改动核心链路
- [ ] Typecheck / 编译通过

### US-010: 发帖/评论接口接入审核链路
**Description:** 作为开发者，我需要将现有的帖子创建/编辑、评论创建接口接入审核链路，使所有内容经过审核流程。

**Acceptance Criteria:**
- [ ] 帖子创建接口：提交后同步调用审核引擎，根据结果设置 `status`（复用现有字段：1=pending_review, 2=published, 3=rejected, 5=shadow_banned）
- [ ] 帖子编辑接口：内容变更时重新触发审核（status 可能从 2(published) 回到 1(pending_review)）
- [ ] 被驳回/下架的内容允许作者编辑后重新提交，编辑后 status 重置为 `1`(pending_review)
- [ ] 评论创建接口：同帖子逻辑
- [ ] 自动放行的内容对用户无感知（响应正常）
- [ ] 被拦截的内容返回友好提示（如"内容正在审核中，请稍候"）
- [ ] 灰区内容返回正常响应，用户不感知进入人审
- [ ] 集成测试覆盖三种决策路径
- [ ] Typecheck / 编译通过

### US-011: 规则命中统计与效果评估
**Description:** 作为运营人员，我需要查看每条规则的命中统计数据，以便评估规则有效性、发现误杀并优化规则质量。

**Acceptance Criteria:**
- [ ] 新建规则统计表 `moderation_rule_stats`，字段包含：rule_id、hit_total、approve_total、review_total、reject_total、shadow_total、updated_at
- [ ] 规则命中时同步更新对应统计计数（rule_id + decision 维度）
- [ ] `GET /api/admin/moderation/rules/{id}/stats` — 返回该规则的累计命中统计
- [ ] `GET /api/admin/moderation/rules/stats` — 批量返回所有规则的统计摘要（支持按 hit_total 降序排列）
- [ ] 规则新增时同步插入 `moderation_rule_stats` 初始行（所有计数为 0）
- [ ] 规则命中时使用 `INSERT ... ON DUPLICATE KEY UPDATE` 保证即使初始行缺失也能正确累加
- [ ] 统计更新采用原子操作（如 `UPDATE ... SET hit_total = hit_total + 1`），避免并发丢失
- [ ] 接口鉴权：仅 ADMIN 角色
- [ ] 单元测试覆盖计数累加、并发安全
- [ ] Typecheck / 编译通过

## 4. Functional Requirements / 功能需求

- **FR-1:** 帖子表**复用现有 `status` 字段**（int），在现有值 0-4 基础上新增 `5 = shadow_banned`；评论表**新增 `status` 字段**（int：0=pending_review, 1=approved, 2=rejected, 3=shadow_banned, 4=taken_down，默认 0）
- **FR-2:** 新建审核规则表 `moderation_rule`，字段包含：id、rule_type（keyword/regex/blacklist）、pattern、weight_score、enabled、description、created_at、updated_at、deleted（逻辑删除）
- **FR-3:** 新建审核队列表 `moderation_queue`，字段包含：id、target_id、target_type（post/comment）、risk_score、matched_rules_snapshot（JSON）、user_profile_snapshot（JSON）、status、created_at、reviewed_at、reviewer_id
- **FR-4:** 新建审计日志表 `moderation_audit_log`，字段包含：id、target_id、target_type、action、operator_id、operator_type（SYSTEM/ADMIN）、reason、matched_rules_snapshot、risk_score、created_at
- **FR-5:** 规则引擎按优先级依次执行关键词匹配、正则匹配、黑名单检查，累加命中规则的权重分
- **FR-6:** 用户画像加权因子从数据库读取，与文本基础分相乘得到最终风险分（封顶 100）
- **FR-7:** 阈值配置存储在数据库（键值对），支持管理员在线调整
- **FR-8:** 同步链路处理自动放行与自动拦截；灰区内容异步写入 `moderation_queue`，状态为 PENDING_REVIEW
- **FR-9:** 影子发布内容仅对发布者本人可见，其他用户的查询结果自动过滤
- **FR-10:** 审核操作 API 仅限 ADMIN 角色访问，操作需附带理由
- **FR-11:** 所有审核动作（含系统自动判定）均写入 `moderation_audit_log`，日志仅追加不可修改
- **FR-12:** 规则变更后自动刷新 Redis 缓存，保证规则实时生效
- **FR-13:** 图片审核通过策略模式预留扩展接口，第一期空实现直接放行
- **FR-14:** 被驳回（REJECTED）或下架（TAKEN_DOWN）的内容允许作者编辑后重新提交，编辑保存时状态重置为 `PENDING_REVIEW` 并重新进入审核链路
- **FR-15:** 文本匹配前进行基础归一化预处理：去除多余空格、统一大小写、全角转半角；高级对抗（拼音/变体）放二期
- **FR-16:** 新建规则统计表 `moderation_rule_stats`（rule_id, hit_total, approve_total, review_total, reject_total, shadow_total, updated_at）；规则新增时同步插入初始行（计数为 0），命中时使用 `INSERT ... ON DUPLICATE KEY UPDATE` 原子累加计数
- **FR-17:** 提供规则统计查询接口，支持单条规则和批量规则的命中数据查询

## 5. Non-Goals / 不在范围内

- **不做** 图片内容检测（仅留接口占位）
- **不做** 第三方内容安全服务对接（如阿里绿网、腾讯天御等）
- **不做** AI/NLP 模型语义审核
- **不做** 用户申诉与自助解封流程
- **不做** 审核数据统计仪表盘（一期仅提供规则命中累计统计 API）
- **不做** 批量审核操作
- **不做** 审核任务分配与工作流（如指定审核员）
- **不做** 消息通知（如审核结果推送给用户）
- **不做** 影子发布自动过期（二期通过定时任务实现）
- **不做** 审核队列积压告警（二期加入通知机制）
- **不做** 高级文本对抗（拼音/变体/谐音绕过检测，二期实现）
- **不做** 分日/分周的规则命中趋势统计（一期仅累计值）

## 6. Design Considerations / 设计考量

### 数据库设计
- 审核状态字段加索引，支持高效的列表查询过滤
- `matched_rules_snapshot` 使用 JSON 类型存储命中规则快照，与规则表解耦，避免规则变更影响历史记录
- 审计日志表采用追加模式，不建立 UPDATE/DELETE 权限

### 架构设计
- 审核引擎采用**策略模式**（Strategy Pattern），按内容类型分发到不同的审核策略实现
- 规则引擎采用**责任链模式**（Chain of Responsibility），关键词 → 正则 → 黑名单依次执行
- 规则缓存使用 **Redis**（项目已引入 `spring-boot-starter-data-redis`），规则变更时主动删除对应缓存 key

### 管理页面
- 基于现有管理后台框架搭建简易审核列表页
- 列表项高亮显示高风险内容（风险分 ≥ 70 红色标记）
- 操作按钮内联在每行，减少点击层级

## 7. Technical Considerations / 技术考量

### 依赖与集成
- 帖子服务与评论服务现有接口需增加审核拦截逻辑
- 审核模块作为独立 Service 层，被帖子/评论 Service 调用
- 规则缓存使用 **Redis**（项目已引入 `spring-boot-starter-data-redis`），规则变更时主动删除缓存 key

### 性能要求
- 同步审核链路（规则引擎 + 画像加权）总耗时 < 300ms
- 审核队列查询接口响应 < 500ms（分页，单页 20 条）

### 数据库迁移
- 需要新建 4 张表：`moderation_rule`、`moderation_queue`、`moderation_audit_log`、`moderation_rule_stats`
- **帖子表**：复用现有 `status` 字段，新增值 `5 = shadow_banned`，**不新增字段**
- **评论表**：新增 `status` 字段（int，默认 0）
- 迁移脚本存量数据处理：**不回溯审核**，存量帖子保持原 status 不变，存量评论 `status` 默认设为 `1`(approved)

### 并发与幂等
- 审核操作接口需保证幂等：同一内容多次提交相同操作不应报错
- 乐观锁或状态机校验防止并发审核冲突（如两个管理员同时操作同一内容）

## 8. Success Metrics / 成功指标

- 所有新发帖子和评论 100% 经过审核链路
- 自动放行率 ≥ 60%（减少人工审核工作量）
- 同步审核链路 P99 延迟 < 300ms
- 审计日志完整率 100%（每条审核动作均有日志）
- 管理员平均审核单条内容耗时 < 30 秒（从打开到操作完成）

## 9. Open Questions / 待确认事项

所有一期问题已确认，决策记录如下：

| # | 问题 | 决策 |
|---|------|------|
| 1 | 存量数据是否回溯审核 | **不回溯**，存量默认 APPROVED，仅新内容生效 |
| 2 | 驳回/下架内容能否编辑再提交 | **允许**，编辑后状态重置为 PENDING_REVIEW |
| 3 | 关键词是否支持模糊/拼音/变体对抗 | **一期不做**，仅基础归一化（去空格/大小写/全半角）+ 正则；二期扩展 |
| 4 | 画像加权系数初始值 | 使用**经验值**（注册<7天 ×1.5，违规≥3次 ×2.0），存配置表可动态调整 |
| 5 | 影子发布是否自动过期 | **一期不做**，避免引入定时任务；二期实现 |
| 6 | 审核队列积压告警 | **一期不做**，仅后台高亮高风险项；二期加告警 |
