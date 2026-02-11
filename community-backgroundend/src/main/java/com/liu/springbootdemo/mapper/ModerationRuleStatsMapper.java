package com.liu.springbootdemo.mapper;

import com.liu.springbootdemo.POJO.entity.ModerationRuleStats;
import com.liu.springbootdemo.POJO.vo.ModerationRuleStatsVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 审核规则命中统计 Mapper
 */
@Mapper
public interface ModerationRuleStatsMapper {

    /**
     * 原子递增规则命中次数（INSERT ON DUPLICATE KEY UPDATE）
     */
    @Insert("INSERT INTO moderation_rule_stats (rule_id, hit_count, last_hit_at) " +
            "VALUES (#{ruleId}, 1, NOW()) " +
            "ON DUPLICATE KEY UPDATE hit_count = hit_count + 1, last_hit_at = NOW()")
    void incrementHitCount(@Param("ruleId") Long ruleId);

    /**
     * 根据规则 ID 查询统计
     */
    @Select("SELECT s.rule_id, r.rule_type, r.pattern, r.description, r.enabled, " +
            "s.hit_count, s.last_hit_at " +
            "FROM moderation_rule_stats s " +
            "INNER JOIN moderation_rule r ON s.rule_id = r.id " +
            "WHERE s.rule_id = #{ruleId} AND r.deleted = 0")
    ModerationRuleStatsVO findByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 查询所有规则的统计摘要（包含无命中的规则）
     */
    @Select("SELECT r.id AS rule_id, r.rule_type, r.pattern, r.description, r.enabled, " +
            "COALESCE(s.hit_count, 0) AS hit_count, s.last_hit_at " +
            "FROM moderation_rule r " +
            "LEFT JOIN moderation_rule_stats s ON r.id = s.rule_id " +
            "WHERE r.deleted = 0 " +
            "ORDER BY hit_count DESC")
    List<ModerationRuleStatsVO> findAllStats();
}
