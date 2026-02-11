package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.entity.ModerationAuditLog;
import com.liu.springbootdemo.POJO.vo.ModerationAuditLogVO;
import com.liu.springbootdemo.mapper.ModerationAuditLogMapper;
import com.liu.springbootdemo.service.ModerationAuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核审计日志服务实现
 */
@Service
@Slf4j
public class ModerationAuditLogServiceImpl implements ModerationAuditLogService {

    @Autowired
    private ModerationAuditLogMapper auditLogMapper;

    @Override
    public void logAutoAction(Long targetId, String targetType, String actionType,
                              String hitRules, int riskScore) {
        ModerationAuditLog auditLog = new ModerationAuditLog();
        auditLog.setTargetId(targetId);
        auditLog.setTargetType(targetType);
        auditLog.setActionType(actionType);
        auditLog.setOperatorId(null);
        auditLog.setOperatorName("SYSTEM");
        auditLog.setReason(null);
        auditLog.setHitRules(hitRules);
        auditLog.setRiskScore(riskScore);

        try {
            auditLogMapper.insert(auditLog);
            log.info("自动审核日志已记录: targetId={}, targetType={}, action={}, riskScore={}",
                    targetId, targetType, actionType, riskScore);
        } catch (Exception e) {
            log.error("写入自动审核日志失败: targetId={}, targetType={}", targetId, targetType, e);
        }
    }

    @Override
    public void logManualAction(Long targetId, String targetType, String actionType,
                                Long operatorId, String operatorName, String reason,
                                String hitRules, int riskScore) {
        ModerationAuditLog auditLog = new ModerationAuditLog();
        auditLog.setTargetId(targetId);
        auditLog.setTargetType(targetType);
        auditLog.setActionType(actionType);
        auditLog.setOperatorId(operatorId);
        auditLog.setOperatorName(operatorName);
        auditLog.setReason(reason);
        auditLog.setHitRules(hitRules);
        auditLog.setRiskScore(riskScore);

        try {
            auditLogMapper.insert(auditLog);
            log.info("人工审核日志已记录: targetId={}, targetType={}, action={}, operator={}",
                    targetId, targetType, actionType, operatorName);
        } catch (Exception e) {
            log.error("写入人工审核日志失败: targetId={}, targetType={}", targetId, targetType, e);
        }
    }

    @Override
    public List<ModerationAuditLogVO> getAuditLogs(Long targetId, String targetType) {
        return auditLogMapper.findByTarget(targetId, targetType);
    }
}
