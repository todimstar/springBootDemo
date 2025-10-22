```sql
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `comments`;

CREATE TABLE `comments`(
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论主键ID',
  `post_id` BIGINT NOT NULL COMMENT '所属帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '评论者的用户ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论创建时间',
  `ip_address` VARCHAR(45) default null COMMENT '评论时用户ip',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`) COMMENT'为加速查询帖子下的所有评论的索引' ,
  KEY `idx_user_id` (`user_id`) COMMENT '为加速查询用户的评论的索引',
  CONSTRAINT `fk_comments_posts` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  constraint `fk_comments_users` foreign key (`user_id`) references `users` (`id`) ON DELETE cascade ON UPDATE cascade
)

SET FOREIGN_KEY_CHECKS = 1;
```