package com.liu.springbootdemo.POJO.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核审计日志实体（仅INSERT，不可修改）
 */
@Data
public class ModerationAuditLog {
    private Long id;
    private Long targetId;
    private String targetType;
    private String actionType;
    private Long operatorId;
    private String operatorName;
    private String reason;
    private String hitRules;
    private int riskScore;
    private LocalDateTime createdAt;
}
