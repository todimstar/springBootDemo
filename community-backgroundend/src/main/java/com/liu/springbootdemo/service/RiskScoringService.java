package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.dto.RiskScoreResult;
import com.liu.springbootdemo.POJO.entity.User;

/**
 * 内容风险评分引擎接口
 */
public interface RiskScoringService {

    /**
     * 对文本内容进行风险评分（纯文本评分，不含用户画像加权）
     *
     * @param text 待评估的文本内容
     * @return 风险评分结果（含命中规则明细）
     */
    RiskScoreResult evaluate(String text);

    /**
     * 对文本内容进行风险评分，并根据用户画像进行加权
     * 最终评分 = 文本基础分 × 用户画像加权系数，上限封顶 100
     *
     * @param text 待评估的文本内容
     * @param user 发布内容的用户
     * @return 加权后的风险评分结果
     */
    RiskScoreResult evaluateWithUserProfile(String text, User user);
}
