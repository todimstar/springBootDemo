package com.liu.springbootdemo.POJO.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户风险因子加权配置实体
 */
@Data
public class UserRiskFactorConfig {
    private Long id;
    private String factorName;   // registration_days / violation_count / level
    private String operator;     // LT / LTE / GT / GTE / EQ
    private double threshold;
    private double multiplier;   // 加权系数
    private boolean enabled;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
