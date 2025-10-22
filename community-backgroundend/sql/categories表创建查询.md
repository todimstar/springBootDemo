SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF	EXISTS `categories`;

CREATE TABLE `categories` (
	`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分区ID',
	`name` VARCHAR ( 50 ) NOT NULL COMMENT '分区名称',
	`description` VARCHAR ( 200 ) DEFAULT '' COMMENT '分区描述',
	`icon` VARCHAR ( 255 ) DEFAULT NULL COMMENT '分区图标URL',
	`post_count` INT DEFAULT 0 COMMENT '分区帖子数量',
	`sort_order` INT DEFAULT 0 COMMENT '分区权重，越大越前面',
	`is_active` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
	`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创造时间',
	PRIMARY KEY ( `id` ),
	UNIQUE KEY `uk_name` ( `name` ),
	KEY `idx_sort_order` ( `sort_order` ) 
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分区表';

SET FOREIGN_KEY_CHECKS = 1;
INSERT INTO `categories` ( `name`, `description`, `sort_order`)
VALUES
	( '技术分享', '编程技术、算法、架构设计等技术讨论', 100 ),
	( '职场生活', '面试经验、职业规划、工作感悟', 90 ),
	( '问答求助', '技术问题求助、代码debug', 80 ),
	( '工具资源', '开发工具推荐、学习资源分享', 70 ),
	( '闲聊灌水', '轻松话题、日常闲聊', 60 );