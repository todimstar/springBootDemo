package com.liu.springbootdemo.POJO.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核规则 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModerationRuleVO {
    private Long id;
    private String ruleType;
    private String pattern;
    private int weightScore;
    private boolean enabled;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
