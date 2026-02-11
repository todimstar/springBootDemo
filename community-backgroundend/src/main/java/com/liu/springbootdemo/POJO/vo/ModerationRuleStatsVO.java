package com.liu.springbootdemo.POJO.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则统计 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModerationRuleStatsVO {
    private Long ruleId;
    private String ruleType;
    private String pattern;
    private String description;
    private boolean enabled;
    private long hitCount;
    private LocalDateTime lastHitAt;
}
