package com.liu.springbootdemo.POJO.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条规则命中结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleHitResult {
    private Long ruleId;          // 命中的规则ID
    private String ruleType;      // 规则类型：keyword/regex/blacklist
    private String matchedContent; // 命中的文本片段
    private int weightScore;      // 该规则的权重分
}
