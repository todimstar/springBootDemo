package com.liu.springbootdemo.POJO.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核审计日志 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModerationAuditLogVO {
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
