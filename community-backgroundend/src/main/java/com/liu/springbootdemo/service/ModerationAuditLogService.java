package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.vo.ModerationAuditLogVO;

import java.util.List;

/**
 * 审核审计日志服务
 */
public interface ModerationAuditLogService {

    /**
     * 记录自动审核日志（操作人为 SYSTEM）
     *
     * @param targetId   目标ID
     * @param targetType 目标类型（post/comment）
     * @param actionType 操作类型（auto_approve/auto_reject/pending_review）
     * @param hitRules   命中规则快照(JSON)
     * @param riskScore  风险评分快照
     */
    void logAutoAction(Long targetId, String targetType, String actionType, String hitRules, int riskScore);

    /**
     * 记录人工审核日志
     *
     * @param targetId     目标ID
     * @param targetType   目标类型（post/comment）
     * @param actionType   操作类型（approve/reject/takedown/shadow_ban）
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @param reason       审核理由
     * @param hitRules     命中规则快照(JSON)
     * @param riskScore    风险评分快照
     */
    void logManualAction(Long targetId, String targetType, String actionType,
                         Long operatorId, String operatorName, String reason,
                         String hitRules, int riskScore);

    /**
     * 查询目标的审计轨迹
     *
     * @param targetId   目标ID
     * @param targetType 目标类型（post/comment）
     * @return 审计日志列表，按时间升序
     */
    List<ModerationAuditLogVO> getAuditLogs(Long targetId, String targetType);
}
