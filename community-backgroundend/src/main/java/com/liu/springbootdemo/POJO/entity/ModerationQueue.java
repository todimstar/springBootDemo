package com.liu.springbootdemo.POJO.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核队列实体
 */
@Data
public class ModerationQueue {
    private Long id;
    private Long targetId;
    private String targetType;
    private Long authorId;
    private String contentSummary;
    private int riskScore;
    private String hitRules;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
