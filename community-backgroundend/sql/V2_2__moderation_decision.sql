-- ========================================
-- V2.2 审核决策阈值配置表 + 审核队列表
-- US-003: 审核决策与状态流转
-- ========================================

-- 审核阈值配置表
CREATE TABLE IF NOT EXISTS moderation_threshold_config (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key  VARCHAR(64) NOT NULL UNIQUE COMMENT '配置键: auto_approve_max / auto_reject_min',
    config_value INT NOT NULL COMMENT '阈值分数',
    description VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核决策阈值配置';

-- 初始阈值：score < 30 → 自动放行，30 ≤ score < 70 → 送人审，score ≥ 70 → 自动拦截
INSERT INTO moderation_threshold_config (config_key, config_value, description) VALUES
    ('auto_approve_max', 30, '自动放行上限（不含），低于此分数自动放行'),
    ('auto_reject_min', 70, '自动拦截下限（含），大于等于此分数自动拦截');

-- 审核队列表（灰区内容进入人工审核队列）
CREATE TABLE IF NOT EXISTS moderation_queue (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_id       BIGINT NOT NULL COMMENT '目标ID（帖子ID或评论ID）',
    target_type     VARCHAR(20) NOT NULL DEFAULT 'post' COMMENT '目标类型: post / comment',
    author_id       BIGINT NOT NULL COMMENT '作者用户ID',
    content_summary VARCHAR(500) DEFAULT NULL COMMENT '内容摘要',
    risk_score      INT NOT NULL DEFAULT 0 COMMENT '风险评分',
    hit_rules       TEXT DEFAULT NULL COMMENT '命中规则摘要(JSON)',
    status          VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '队列状态: pending / reviewed',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_risk_score (risk_score DESC),
    INDEX idx_target (target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核队列';
