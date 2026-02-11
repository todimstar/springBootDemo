package com.liu.springbootdemo.POJO.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本风险评分结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskScoreResult {
    /** 最终风险分 0-100 */
    private int riskScore;
    /** 所有命中规则的详情列表 */
    private List<RuleHitResult> hitResults = new ArrayList<>();

    /**
     * 构建无命中的结果
     */
    public static RiskScoreResult safe() {
        return new RiskScoreResult(0, new ArrayList<>());
    }
}
