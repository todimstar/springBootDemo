-- =============================================
-- V2.1: 用户画像风险加权 - 配置表 + users 字段扩展
-- =============================================

-- 1. users 表新增违规次数字段
ALTER TABLE `users` ADD COLUMN `violation_count` int NOT NULL DEFAULT 0 COMMENT '历史违规次数' AFTER `level`;

-- 2. 用户风险因子加权配置表
CREATE TABLE IF NOT EXISTS `user_risk_factor_config` (
  `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `factor_name` varchar(50) NOT NULL COMMENT '画像因子名称: registration_days / violation_count / level',
  `operator`    varchar(10) NOT NULL COMMENT '比较运算符: LT / LTE / GT / GTE / EQ',
  `threshold`   double      NOT NULL COMMENT '阈值',
  `multiplier`  double      NOT NULL DEFAULT 1.0 COMMENT '加权系数',
  `enabled`     tinyint(1)  NOT NULL DEFAULT 1 COMMENT '是否启用: 1=启用, 0=禁用',
  `description` varchar(255) DEFAULT '' COMMENT '配置描述',
  `created_at`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_factor_name` (`factor_name`),
  INDEX `idx_enabled` (`enabled`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户风险因子加权配置表';

-- 3. 初始加权规则种子数据
INSERT INTO `user_risk_factor_config` (`factor_name`, `operator`, `threshold`, `multiplier`, `enabled`, `description`) VALUES
('registration_days', 'LT', 7, 1.5, 1, '注册天数 < 7天 → ×1.5'),
('violation_count', 'GTE', 3, 2.0, 1, '历史违规次数 >= 3次 → ×2.0'),
('level', 'LT', 3, 1.2, 1, '账号等级 < 3 → ×1.2');
