-- ========================================
-- V2.3 审核审计日志表
-- US-007: 审计日志记录与查询
-- ========================================

CREATE TABLE IF NOT EXISTS moderation_audit_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_id       BIGINT NOT NULL COMMENT '目标ID（帖子ID或评论ID）',
    target_type     VARCHAR(20) NOT NULL DEFAULT 'post' COMMENT '目标类型: post / comment',
    action_type     VARCHAR(30) NOT NULL COMMENT '操作类型: auto_approve / auto_reject / pending_review / approve / reject / takedown / shadow_ban',
    operator_id     BIGINT DEFAULT NULL COMMENT '操作人ID，自动审核为 NULL',
    operator_name   VARCHAR(50) NOT NULL DEFAULT 'SYSTEM' COMMENT '操作人名称，自动审核标记为 SYSTEM',
    reason          VARCHAR(500) DEFAULT NULL COMMENT '审核理由',
    hit_rules       TEXT DEFAULT NULL COMMENT '命中规则快照(JSON)',
    risk_score      INT NOT NULL DEFAULT 0 COMMENT '风险评分快照',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_target (target_id, target_type),
    INDEX idx_operator (operator_id),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核审计日志（仅INSERT，不可修改）';
