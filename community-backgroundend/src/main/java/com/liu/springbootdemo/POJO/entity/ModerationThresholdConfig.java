package com.liu.springbootdemo.POJO.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核决策阈值配置实体
 */
@Data
public class ModerationThresholdConfig {
    private Long id;
    private String configKey;
    private int configValue;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
