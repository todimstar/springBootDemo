package com.liu.springbootdemo.POJO.dto;

import com.liu.springbootdemo.common.enums.ModerationDecision;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核决策结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModerationDecisionResult {
    /** 最终风险评分 */
    private int riskScore;
    /** 审核决策 */
    private ModerationDecision decision;
    /** 对应的帖子状态值 */
    private int postStatus;
    /** 风险评分详情 */
    private RiskScoreResult scoreResult;
}
