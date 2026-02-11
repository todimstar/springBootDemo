package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.dto.RiskScoreResult;

/**
 * 内容风险评分引擎接口
 */
public interface RiskScoringService {

    /**
     * 对文本内容进行风险评分
     *
     * @param text 待评估的文本内容
     * @return 风险评分结果（含命中规则明细）
     */
    RiskScoreResult evaluate(String text);
}
