/*
 Navicat Premium Data Transfer

 Source Server         : 113.45.27.27
 Source Server Type    : MySQL
 Source Server Version : 80035
 Source Host           : 113.45.27.27:3306
 Source Schema         : springboot_db

 Target Server Type    : MySQL
 Target Server Version : 80035
 File Encoding         : 65001

 Date: 05/02/2026 23:45:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '分区ID',
  `name` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分区名称',
  `description` varchar(220) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '分区描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分区图标URL',
  `post_count` int(0) NULL DEFAULT 0 COMMENT '分区帖子数量',
  `sort_order` int(0) NULL DEFAULT 0 COMMENT '分区权重，越大越前面',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创造时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name`) USING BTREE,
  INDEX `idx_active_sort`(`is_active`, `sort_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分区表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '技术分享', '编程技术、算法、架构设计等技术讨论', NULL, 0, 100, 1, '2025-10-10 13:31:33');
INSERT INTO `categories` VALUES (2, '职场生活', '面试经验、职业规划、工作感悟', NULL, 0, 90, 1, '2025-10-10 13:31:33');
INSERT INTO `categories` VALUES (3, '问答求助', '技术问题求助、代码debug', NULL, 0, 80, 1, '2025-10-10 13:31:33');
INSERT INTO `categories` VALUES (4, '工具资源', '开发工具推荐、学习资源分享', NULL, 0, 70, 1, '2025-10-10 13:31:33');
INSERT INTO `categories` VALUES (5, '闲聊灌水', '轻松话题、日常闲聊', NULL, 0, 60, 1, '2025-10-10 13:31:33');
INSERT INTO `categories` VALUES (6, '浓浓缘什', '农和原神', NULL, 0, 0, 0, '2025-10-21 11:18:15');
INSERT INTO `categories` VALUES (7, 'i珂TV', 'ee，被害者，嘉豪', NULL, 0, 911, 1, '2025-10-22 15:13:41');
INSERT INTO `categories` VALUES (8, '芽衣大厂', '尤诺，不要，华为,OD', NULL, 0, 911, 1, '2025-10-22 17:39:58');
INSERT INTO `categories` VALUES (9, 'A测试开关分区', '你说，能赢吗', NULL, 0, 911, 0, '2025-10-22 17:45:22');
INSERT INTO `categories` VALUES (12, '等着被删的分区12', '任尔东西南北风12', NULL, 0, 911, 1, '2025-10-22 18:02:19');
INSERT INTO `categories` VALUES (14, '测试检验Valid需不需要Validated的分区14-好像不需要', '嘉豪梨花带雨14', NULL, 0, 911, 0, '2025-10-22 20:25:59');
INSERT INTO `categories` VALUES (15, '测试分区创建功能', '测试分区创建功能的描述', 'https://avatars.githubusercontent.com/u/36943307', 0, 900, 1, '2025-12-21 21:51:32');
INSERT INTO `categories` VALUES (16, '测试分区创建功能-修复createTime回显', '测试分区创建功能的描述', 'https://avatars.githubusercontent.com/u/36943307', 0, 900, 1, '2025-12-21 22:06:42');
INSERT INTO `categories` VALUES (17, '测试分区创建功能-修复createTime回显-waing被删', '测试分区创建功能的描述', 'https://avatars.githubusercontent.com/u/36943307', 0, 900, 1, '2025-12-21 22:24:14');
INSERT INTO `categories` VALUES (18, '貊敬阳', '过问记很。最济书酸完具要实强部。话看由点做新。除表参近只任。', 'https://avatars.githubusercontent.com/u/15309120', 0, 114514, 1, '2025-12-30 22:53:55');
INSERT INTO `categories` VALUES (19, '114514技术交流-waiting被删', '113讨论各种技术相关的话题-waiting被删', '/images/icons/tech.png', 0, 114514, 1, '2025-12-30 22:54:17');
INSERT INTO `categories` VALUES (20, '114514技术交流-waiting被删2', '113讨论各种技术相关的话题-waiting被删', '/images/icons/tech.png', 0, 114514, 1, '2025-12-30 22:54:20');

-- ----------------------------
-- Table structure for collects
-- ----------------------------
DROP TABLE IF EXISTS `collects`;
CREATE TABLE `collects`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint(0) NOT NULL COMMENT '收藏用户ID',
  `post_id` bigint(0) NOT NULL COMMENT '帖子ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id`, `post_id`) USING BTREE COMMENT '唯一键防止重复收藏',
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE,
  CONSTRAINT `fk_collects_posts` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_collects_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collects
-- ----------------------------

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '评论主键ID',
  `post_id` bigint(0) NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint(0) NOT NULL COMMENT '评论者的用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '评论创建时间',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论时用户ip',
  `parent_id` bigint(0) NULL DEFAULT NULL COMMENT '父评论ID',
  `reply_to_user_id` bigint(0) NULL DEFAULT NULL COMMENT '回复给谁',
  `like_count` int(0) NULL DEFAULT 0 COMMENT '点赞数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE COMMENT '为加速查询帖子下的所有评论的索引',
  INDEX `idx_user_id`(`user_id`) USING BTREE COMMENT '为加速查询用户的评论的索引',
  INDEX `fk_comments_parent`(`parent_id`) USING BTREE,
  INDEX `fk_comments_reply_to`(`reply_to_user_id`) USING BTREE,
  CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comments_posts` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comments_reply_to` FOREIGN KEY (`reply_to_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_comments_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comments
-- ----------------------------
INSERT INTO `comments` VALUES (3, 26, 11, '我是2号的人机评论1', '2025-09-20 22:38:58', NULL, NULL, NULL, 0, 0);
INSERT INTO `comments` VALUES (4, 26, 11, '我是2号的人机评论2', '2025-09-20 22:39:03', NULL, NULL, NULL, 0, 0);
INSERT INTO `comments` VALUES (5, 26, 11, '我是2号的人机评论2', '2025-09-20 22:39:35', NULL, NULL, NULL, 0, 0);
INSERT INTO `comments` VALUES (9, 5, 11, '验证user2可用', '2025-10-22 17:50:02', NULL, NULL, NULL, 0, 0);

-- ----------------------------
-- Table structure for follows
-- ----------------------------
DROP TABLE IF EXISTS `follows`;
CREATE TABLE `follows`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '关注关系id',
  `follower_id` bigint(0) NOT NULL COMMENT '关注者id',
  `followee_id` bigint(0) NOT NULL COMMENT '被关注者id',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '关注时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `follower_id`(`follower_id`, `followee_id`) USING BTREE,
  INDEX `idx_follower`(`follower_id`) USING BTREE,
  INDEX `idx_followee`(`followee_id`) USING BTREE,
  CONSTRAINT `fk_follows_followee` FOREIGN KEY (`followee_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_follows_follower` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '关注关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of follows
-- ----------------------------

-- ----------------------------
-- Table structure for likes
-- ----------------------------
DROP TABLE IF EXISTS `likes`;
CREATE TABLE `likes`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint(0) NOT NULL COMMENT '点赞用户ID',
  `target_id` bigint(0) NOT NULL COMMENT '目标ID(帖子/评论)',
  `target_type` tinyint(0) NOT NULL COMMENT '类型：1帖子，2评论',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id`, `target_id`, `target_type`) USING BTREE COMMENT '防止重复点赞,唯一键',
  INDEX `idx_target`(`target_id`, `target_type`) USING BTREE,
  CONSTRAINT `fk_likes_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of likes
-- ----------------------------

-- ----------------------------
-- Table structure for post_images
-- ----------------------------
DROP TABLE IF EXISTS `post_images`;
CREATE TABLE `post_images`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '贴图关系id',
  `post_id` bigint(0) NULL DEFAULT NULL COMMENT '所属帖子-未绑定时为空',
  `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片访问url',
  `object_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Minio中的对象名',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '0为TEMP,1为USED',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间-未知用途',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_time`(`status`, `create_time`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE COMMENT '用于按帖子找图片时'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子图片表-区分临时和正在帖子中使用的图片' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_images
-- ----------------------------

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
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区名',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `view_count` int(0) NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int(0) NULL DEFAULT 0 COMMENT '点赞数',
  `collect_count` int(0) NULL DEFAULT 0 COMMENT '收藏数',
  `comment_count` int(0) NULL DEFAULT 0 COMMENT '评论数',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态：0草稿,1待审核,2已发布,3已拒绝,4已删除',
  `is_pinned` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `is_essence` tinyint(1) NULL DEFAULT 0 COMMENT '是否加精',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布者IP',
  `summary` varchar(305) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子简介，用于点击前展示',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE COMMENT '为作者ID创建索引以优化查询',
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_view_count`(`view_count`) USING BTREE,
  INDEX `idx_like_count`(`like_count`) USING BTREE,
  INDEX `idx_hot`(`like_count`, `comment_count`, `view_count`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of posts
-- ----------------------------
INSERT INTO `posts` VALUES (3, 10, '第一篇帖子（白垩修改版）', '悄悄修改第一篇内容（白垩修改的）', '2025-09-04 20:46:23', '2025-10-13 11:03:41', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (4, 10, '我的第一篇帖子(修改版)', '修改了', '2025-09-04 20:57:27', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (5, 10, '修改帖子', '天天通天塔', '2025-09-05 09:11:45', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (6, 10, '我的第一篇帖子', '这是通过JWT认证后发布的内容！', '2025-09-15 09:53:04', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (7, 11, '我2号的第一篇帖子', '完成了jwt过期返回', '2025-09-15 09:54:19', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (8, 11, '我2号的第一篇帖子', '3号修改一下帖子然后创建评论', '2025-09-15 10:20:30', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (9, 11, '我2号的第二篇帖子', '明了了Security中过滤处理未认证的情况', '2025-09-15 11:34:25', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (10, 11, '我2号的第三篇帖子', '修复了创造帖子时返回的不是数据库的实时帖子的情况，在Service之前查询了数据库一次', '2025-09-15 11:37:48', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (11, 10, '我2号的第三篇帖子', '试图使用没有auth的创建测试', '2025-09-15 13:22:31', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (12, 11, '我2号的第三篇帖子', '试图使用没有auth的创建测试', '2025-09-15 13:22:44', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (13, 11, '我2号的第三篇帖子', '试图使用没有auth的创建测试', '2025-09-15 13:23:26', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (14, 11, '我2号的第三篇帖子', '试图使用没有auth的创建测试', '2025-09-15 13:23:39', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (15, 11, '我2号的第三篇帖子', '试图使用没有auth的创建测试', '2025-09-15 13:24:06', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (16, 11, '我2号的第三篇帖子', '试图使用没有auth的创建测试，10秒前的token', '2025-09-15 13:25:10', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (17, 11, '我2号的第三篇帖子', '试图使用没有auth的创建测试，10秒前的token遇到10秒jwt检查', '2025-09-15 13:25:39', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (19, 11, '我2号的测试删除帖子', '喂我花生2', '2025-09-15 23:08:50', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (21, 11, '我2号的测试删除帖子', '喂我花生3', '2025-09-16 09:27:39', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (22, 11, '我2号的测试删除帖子', '喂我花生4', '2025-09-16 09:27:54', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (23, 11, '我2号的测试删除帖子', '喂我花生5', '2025-09-16 09:27:58', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (24, 11, '我2号的测试删除帖子', '喂我花生5', '2025-09-16 09:28:50', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (25, 11, '我2号的测试删除帖子', '喂我花生25', '2025-09-16 09:29:07', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (26, 11, '我2号的测试删除帖子', '喂我花生25', '2025-09-16 09:29:33', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (28, 11, '我2号的测试标题不能为空已测试', '内容不能为空已测试', '2025-09-16 09:30:37', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (29, 10, '我1号的测试要被2号删掉的29', '山里面', '2025-09-16 09:32:50', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (30, 10, '我1号的测试要被2号删掉的30', '山里面', '2025-09-16 09:33:31', '2025-10-13 11:04:01', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (31, 36, '3号的帖子1', '3号修改一下帖子然后创建评论', '2025-09-19 12:13:29', '2025-12-30 02:18:43', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (32, 36, '剪伟大啃篮子飞机热仅仅顺便说一下聪明哎呀', 'sunt do', '2025-12-29 00:01:22', '2026-01-05 00:14:04', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (33, 36, '剪伟大啃篮子飞机热仅仅顺便说一下聪明哎呀', 'sunt do', '2025-12-29 00:01:50', '2026-01-05 00:14:04', 5, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (34, 36, '剪伟大啃篮子飞机热仅仅顺便说一下聪明哎呀', 'sunt do', '2025-12-29 00:11:40', '2026-01-05 00:14:04', 8, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (35, 36, '30号晚上写安全教育', 'sunt do C://localhost:8080', '2025-12-30 20:20:47', '2026-01-05 00:14:04', 1, '', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '');
INSERT INTO `posts` VALUES (36, 36, '30号晚上写安全教育-update1', '修改一下分类为2,和coverImage、titile', '2025-12-30 21:19:11', '2026-01-05 00:14:04', 2, '职场生活', 'hei.jpg', 0, 0, 0, 0, 2, 0, 0, NULL, '测试简介');
INSERT INTO `posts` VALUES (39, 37, '31号晚上要跨年？- update categoryId', 'sunt do C://localhost:8080', '2025-12-31 16:58:16', '2026-01-05 00:14:04', 1, '技术分享', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '世上嗯呢有几人渡马年');
INSERT INTO `posts` VALUES (40, 36, '4号了，项目在不进展就没有实习了', '10号前做完啊啊啊啊啊', '2026-01-04 23:51:46', '2026-01-05 00:14:04', 1, '技术分享', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '10号？');
INSERT INTO `posts` VALUES (41, 36, '4号了，项目在不进展就没有实习了', '测试回显时间', '2026-01-05 00:07:24', '2026-01-05 00:15:15', 1, '技术分享', NULL, 0, 0, 0, 0, 0, 0, 0, NULL, '10号？');
INSERT INTO `posts` VALUES (42, 36, '测试状态', '测试1待审核', '2026-01-05 00:09:56', '2026-01-05 00:15:15', 1, '技术分享', NULL, 0, 0, 0, 0, 0, 0, 0, NULL, '10号？');
INSERT INTO `posts` VALUES (43, 36, '测试状态', '测试1待审核', '2026-01-05 00:10:28', '2026-01-05 00:14:04', 1, '技术分享', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '10号？');
INSERT INTO `posts` VALUES (44, 11, '测试user2创建帖子去软删除', '测试1待审核', '2026-01-05 01:10:20', '2026-01-05 01:10:20', 1, '技术分享', NULL, 0, 0, 0, 0, 4, 0, 0, NULL, '10号？');
INSERT INTO `posts` VALUES (45, 11, '测试user2创建帖子去软删除', '测试1待审核', '2026-01-05 01:14:22', '2026-01-05 01:14:22', 1, '技术分享', NULL, 0, 0, 0, 0, 0, 0, 0, NULL, '10号？');
INSERT INTO `posts` VALUES (46, 11, '测试user2创建帖子去软删除', '测试1待审核', '2026-01-05 01:16:23', '2026-01-05 01:16:23', 1, '技术分享', NULL, 0, 0, 0, 0, 1, 0, 0, NULL, '10号？');
INSERT INTO `posts` VALUES (47, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态0', '2026-01-05 18:01:56', '2026-01-05 18:01:56', 1, '技术分享', NULL, 0, 0, 0, 0, 0, 0, 0, NULL, '代号0');
INSERT INTO `posts` VALUES (48, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态0', '2026-01-05 18:02:10', '2026-01-05 18:02:10', 1, '技术分享', NULL, 0, 0, 0, 0, 0, 0, 0, NULL, '代号0');
INSERT INTO `posts` VALUES (49, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态1分区1', '2026-01-05 18:05:09', '2026-01-05 18:05:09', 1, '技术分享', NULL, 0, 0, 0, 0, 1, 0, 0, NULL, '1待审核1分区');
INSERT INTO `posts` VALUES (50, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态1分区2', '2026-01-05 18:05:39', '2026-01-05 18:05:39', 2, '职场生活', NULL, 0, 0, 0, 0, 1, 0, 0, NULL, '1待审核2分区');
INSERT INTO `posts` VALUES (51, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态2分区1', '2026-01-05 18:06:07', '2026-01-05 18:06:07', 1, '技术分享', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '2发布1分区');
INSERT INTO `posts` VALUES (52, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态2分区2', '2026-01-05 18:06:17', '2026-01-05 18:06:17', 2, '职场生活', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '2发布2分区');
INSERT INTO `posts` VALUES (53, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态3分区2', '2026-01-05 18:06:33', '2026-01-05 18:06:33', 2, '职场生活', NULL, 0, 0, 0, 0, 3, 0, 0, NULL, '3拒绝2分区');
INSERT INTO `posts` VALUES (54, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态3分区1', '2026-01-05 18:06:43', '2026-01-05 18:06:43', 1, '技术分享', NULL, 0, 0, 0, 0, 3, 0, 0, NULL, '3拒绝1分区');
INSERT INTO `posts` VALUES (55, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态4分区1', '2026-01-05 18:07:00', '2026-01-05 18:07:00', 1, '技术分享', NULL, 0, 0, 0, 0, 4, 0, 0, NULL, '4删除1分区');
INSERT INTO `posts` VALUES (56, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态4分区2', '2026-01-05 18:07:07', '2026-01-05 18:07:07', 2, '职场生活', NULL, 0, 0, 0, 0, 4, 0, 0, NULL, '4删除2分区');
INSERT INTO `posts` VALUES (57, 9, '测试场景二查看单个帖子详情 (验证 Service 逻辑)', '接口GET /api/posts/{id}，状态4分区3', '2026-01-05 18:07:15', '2026-01-05 18:07:15', 3, '问答求助', NULL, 0, 0, 0, 0, 4, 0, 0, NULL, '4删除3分区');
INSERT INTO `posts` VALUES (58, 9, '测试场景三：删除帖子 (验证软/硬删除)', '接口：DELETE /api/posts/{id}', '2026-01-05 18:29:50', '2026-01-05 18:29:50', 5, '闲聊灌水', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '2发布');
INSERT INTO `posts` VALUES (59, 9, '测试场景三：删除帖子 (验证软/硬删除)', '接口：DELETE /api/posts/{id}', '2026-01-05 18:35:13', '2026-01-06 22:21:55', 1, '技术分享', NULL, 0, 0, 0, 0, 2, 0, 0, NULL, '1审核');
INSERT INTO `posts` VALUES (61, 9, '测试场景三：删除帖子 (验证软/硬删除)', '接口：DELETE /api/posts/{id}', '2026-01-05 18:59:45', '2026-01-05 19:03:11', 5, '闲聊灌水', NULL, 0, 0, 0, 0, 1, 0, 0, NULL, '1审核');
INSERT INTO `posts` VALUES (63, 9, '测试接口ms', '接口：DELETE /api/posts/{id}', '2026-01-06 21:59:48', '2026-01-06 21:59:48', 5, '闲聊灌水', NULL, 0, 0, 0, 0, 4, 0, 0, NULL, '1审核');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户ID, 主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名, 唯一',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码 (Bcrypt加密)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱, 唯一',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '账户创建时间',
  `last_login_time` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ROLE_USER' COMMENT '用户角色',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '/default-avatar.png' COMMENT '头像URL',
  `gender` tinyint(0) NULL DEFAULT 0 COMMENT '性别：0未知,1男,2女',
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '个人简介',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '所在地',
  `points` int(0) NULL DEFAULT 0 COMMENT '积分',
  `level` int(0) NULL DEFAULT 1 COMMENT '用户等级',
  `is_banned` tinyint(1) NULL DEFAULT 0 COMMENT '是否被封禁',
  `ban_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封禁原因',
  `ban_until` datetime(0) NULL DEFAULT NULL COMMENT '封禁截止时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE,
  UNIQUE INDEX `email`(`email`) USING BTREE,
  INDEX `idx_username`(`username`) USING BTREE COMMENT '为用户名创建索引以优化查询'
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (9, 'testuserEncoder1', '$2a$10$lwmA/MwoeZQjAjT9Lb9Ldem5eLqBgtGDsykGOpsfjQTxJd/JOJnqu', 'testuserEncoder1@example.com', '2025-09-03 15:21:07', '2026-01-13 15:11:51', 'ROLE_USER', 'http://localhost:9000/forum-images/avatars/2026/01/19/361c756e-1f72-44e0-b201-ba653ca62e70.jpg', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (11, 'testuserEncoder2', '$2a$10$mcKPjqWMsMTcgtejPzHGeOcyMJSVPA.RoYdj6CnPOG4FKWeMKMcHe', 'testuserEncoder2@example.com', '2025-09-05 23:32:57', '2026-01-13 15:11:39', 'ROLE_USER', 'http://localhost:9000/forum-images/avatars/2026/01/18/cef86a12-deb5-4c10-958f-0986b824f608.jpg', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (36, 'testuserEncoder3', '$2a$10$7Gb68pNEiGgUnGGW82RBpOnWCdJ0jSuwkt1Axg82VE6n.D.9FfBV2', 'testuserEncoder3@example.com', '2025-12-28 15:31:32', '2026-01-13 15:11:21', 'ROLE_ADMIN', 'http://localhost:9000/forum-images/avatars/2026/01/19/f345724b-61b6-47f1-b969-57f6a9e18007.jpg', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (37, 'testuser_001', '$2a$10$U8QwYRCsF51uhhRRaAMb6e8/EmaWUQr9LrSgzIb.h7HWeRb3Iog0u', 'testuser001@example.com', '2025-12-28 15:32:54', '2025-12-28 15:32:54', 'ROLE_USER', '/default-avatar.png', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (38, 'testuser_boundary_gender_0', '$2a$10$QtLdLzeQwvFkWCqL9C2SFOu3TD.ECaMx9uVwwCxMCSS68tgqBc3P6', 'test.gender0@example.com', '2025-12-28 15:32:54', '2025-12-28 15:32:54', 'ROLE_USER', '/default-avatar.png', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (39, 'testuser_negative_points', '$2a$10$KWDbbw0DFs8T6bpvdDJbeuVnl9bXaS.z6PHIWfLfYrTl0aTxrHiMC', 'testuser_negative@example.com', '2025-12-28 15:32:55', '2025-12-28 15:32:55', 'ROLE_USER', '/default-avatar.png', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (40, 'admin', '$2a$10$PKUUyn42yyUfSzU.otqAAuDywC.M0c.4VDq/wxYEy2k1FbIdvnDwi', 'testuser@example.com', '2025-12-28 15:32:55', '2025-12-28 15:32:55', 'ROLE_USER', '/default-avatar.png', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (41, 'testuser_duplicate_email', '$2a$10$sAH3zglvcKaNFVBuk0ex3.uR.HB85sxKXA.9ExDwiSQFYH7BvjACK', 'admin@example.com', '2025-12-28 15:32:55', '2025-12-28 15:32:55', 'ROLE_USER', '/default-avatar.png', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (42, 'testuser_sql_injection', '$2a$10$5WbugKdEf.oNMkvf8tsbuutreCxzemdI1IZgwegwAZGgtP/ogoyMO', 'testuser_sql@example.com', '2025-12-28 15:32:56', '2025-12-28 15:32:56', 'ROLE_USER', '/default-avatar.png', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (43, 'VerEmailUser2', '$2a$10$s//9Whif6ZzI3sA0T6DIMOsRxSjmx1P3kZO/CQtPk3Gvjhg1TnOdm', 'todimstar@outlook.com', '2026-01-10 22:29:40', '2026-01-10 22:29:40', 'ROLE_USER', '/default-avatar.png', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (44, 'VerEmailUser3', '$2a$10$HD1Ev3vRC2PsyaPjaYqcJ.0Uekke/CPug6iSd3hk73UkcOzkgwVtm', 'xxx@outlook.com', '2026-01-10 22:38:14', '2026-01-10 22:38:14', 'ROLE_USER', '/default-avatar.png', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (45, 'VerEmailUser4', '$2a$10$wh7R6Rodej3sp9Gp1jSKCOjn.5J/HKkpOR9HnHSy/8oBv0gS2phfm', 'xxxx@outlook.com', '2026-01-10 22:50:10', '2026-01-10 22:50:10', 'ROLE_USER', '/default-avatar.png', 0, '', '', 0, 1, 0, NULL, NULL, 0);
INSERT INTO `users` VALUES (46, 'VerEmailUser5', '$2a$10$XJh3RoRe6KCq4XJhReUjMO5auWDmZqmh8AonTX3oscYHTIOI2y2sW', 'xxxxx@outlook.com', '2026-01-10 23:00:09', '2026-01-10 23:00:09', 'ROLE_USER', '/default-avatar.png', 0, '', '', 0, 1, 0, NULL, NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;
