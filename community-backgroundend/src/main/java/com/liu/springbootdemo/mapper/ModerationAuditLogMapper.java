package com.liu.springbootdemo.mapper;

import com.liu.springbootdemo.POJO.entity.ModerationAuditLog;
import com.liu.springbootdemo.POJO.vo.ModerationAuditLogVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 审核审计日志 Mapper（仅INSERT + SELECT，不可UPDATE / DELETE）
 */
@Mapper
public interface ModerationAuditLogMapper {

    @Insert("INSERT INTO moderation_audit_log (target_id, target_type, action_type, operator_id, operator_name, reason, hit_rules, risk_score) " +
            "VALUES (#{targetId}, #{targetType}, #{actionType}, #{operatorId}, #{operatorName}, #{reason}, #{hitRules}, #{riskScore})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ModerationAuditLog auditLog);

    @Select("SELECT id, target_id, target_type, action_type, operator_id, operator_name, reason, hit_rules, risk_score, created_at " +
            "FROM moderation_audit_log WHERE target_id = #{targetId} AND target_type = #{targetType} ORDER BY created_at ASC")
    List<ModerationAuditLogVO> findByTarget(@Param("targetId") Long targetId, @Param("targetType") String targetType);
}
