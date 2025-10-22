/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80037
 Source Host           : localhost:3306
 Source Schema         : springboot_db

 Target Server Type    : MySQL
 Target Server Version : 80037
 File Encoding         : 65001

 Date: 13/10/2025 20:38:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for posts
-- ----------------------------
DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '帖子主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '作者的用户ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子内容',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `category_id` bigint(0) NOT NULL COMMENT '分区ID',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `view_count` int(0) NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int(0) NULL DEFAULT 0 COMMENT '点赞数',
  `collect_count` int(0) NULL DEFAULT 0 COMMENT '收藏数',
  `comment_count` int(0) NULL DEFAULT 0 COMMENT '评论数',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态：0草稿,1待审核,2已发布,3已拒绝,4已删除',
  `is_pinned` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `is_essence` tinyint(1) NULL DEFAULT 0 COMMENT '是否加精',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布者IP',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE COMMENT '为作者ID创建索引以优化查询',
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_view_count`(`view_count`) USING BTREE,
  INDEX `idx_like_count`(`like_count`) USING BTREE,
  INDEX `idx_hot`(`like_count`, `comment_count`, `view_count`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
