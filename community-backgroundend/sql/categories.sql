CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分区ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分区名称',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '分区描述',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分区图标URL',
  `post_count` int DEFAULT '0' COMMENT '分区帖子数量',
  `sort_order` int DEFAULT '0' COMMENT '分区权重，越大越前面',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创造时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_active_sort` (`is_active`,`sort_order` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分区表'

