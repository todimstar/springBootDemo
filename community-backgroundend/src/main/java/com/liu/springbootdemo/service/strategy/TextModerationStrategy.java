package com.liu.springbootdemo.service.strategy;

import com.liu.springbootdemo.POJO.dto.RiskScoreResult;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.service.RiskScoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 文本内容审核策略
 * <p>
 * 委托给已有的 {@link RiskScoringService} 完成文本风险评分。
 */
@Component
@Slf4j
public class TextModerationStrategy implements ContentModerationStrategy {

    public static final String CONTENT_TYPE = "text";

    @Autowired
    private RiskScoringService riskScoringService;

    @Override
    public boolean supports(String contentType) {
        return CONTENT_TYPE.equalsIgnoreCase(contentType);
    }

    @Override
    public RiskScoreResult evaluate(String content, User user) {
        if (user != null) {
            return riskScoringService.evaluateWithUserProfile(content, user);
        }
        return riskScoringService.evaluate(content);
    }
}
