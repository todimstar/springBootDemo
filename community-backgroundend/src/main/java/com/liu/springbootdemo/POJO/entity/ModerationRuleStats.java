package com.liu.springbootdemo.POJO.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核规则命中统计实体
 */
@Data
public class ModerationRuleStats {
    private Long id;
    private Long ruleId;
    private long hitCount;
    private LocalDateTime lastHitAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
