package com.liu.springbootdemo.mapper;

import com.liu.springbootdemo.POJO.entity.ModerationQueue;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 审核队列 Mapper
 */
@Mapper
public interface ModerationQueueMapper {

    @Insert("INSERT INTO moderation_queue (target_id, target_type, author_id, content_summary, risk_score, hit_rules, status) " +
            "VALUES (#{targetId}, #{targetType}, #{authorId}, #{contentSummary}, #{riskScore}, #{hitRules}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ModerationQueue queue);
}
