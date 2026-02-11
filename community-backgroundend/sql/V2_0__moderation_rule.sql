-- =============================================
-- V2.0: 内容审核模块 - 审核规则表
-- =============================================

CREATE TABLE IF NOT EXISTS `moderation_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_type` varchar(20) NOT NULL COMMENT '规则类型：keyword/regex/blacklist',
  `pattern` varchar(500) NOT NULL COMMENT '匹配模式（关键词/正则表达式/黑名单项）',
  `weight_score` int NOT NULL DEFAULT 10 COMMENT '命中权重分（0-100）',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0禁用，1启用',
  `description` varchar(255) DEFAULT '' COMMENT '规则描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_rule_type` (`rule_type`),
  INDEX `idx_enabled_deleted` (`enabled`, `deleted`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审核规则表';

-- 插入一些初始规则（用于测试和基础防护）
INSERT INTO `moderation_rule` (`rule_type`, `pattern`, `weight_score`, `enabled`, `description`) VALUES
('keyword', '赌博', 80, 1, '赌博相关关键词'),
('keyword', '色情', 90, 1, '色情相关关键词'),
('keyword', '诈骗', 85, 1, '诈骗相关关键词'),
('keyword', '代开发票', 70, 1, '违法开票广告'),
('regex', '(?i)(v\\s*信|微\\s*信|wx)\\s*[:：]?\\s*[a-zA-Z0-9_]+', 60, 1, '微信号引流正则匹配'),
('regex', '(?i)(QQ|q\\s*q)\\s*[:：]?\\s*\\d{5,12}', 50, 1, 'QQ号引流正则匹配'),
('blacklist', 'bit.ly', 40, 1, '短链接域名黑名单'),
('blacklist', 't.cn', 40, 1, '短链接域名黑名单');
