package com.liu.springbootdemo.POJO.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核规则实体
 */
@Data
public class ModerationRule {
    private Long id;
    private String ruleType;      // keyword / regex / blacklist
    private String pattern;       // 匹配模式
    private int weightScore;      // 命中权重分 0-100
    private boolean enabled;      // 是否启用
    private String description;   // 规则描述
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;      // 逻辑删除
}
