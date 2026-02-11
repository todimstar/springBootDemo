package com.liu.springbootdemo.service.strategy;

import com.liu.springbootdemo.POJO.dto.RiskScoreResult;
import com.liu.springbootdemo.POJO.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核策略分发器
 * <p>
 * 根据内容类型自动路由到对应的 {@link ContentModerationStrategy} 实现。
 * Spring 自动注入所有 ContentModerationStrategy 实现类。
 */
@Service
@Slf4j
public class ModerationStrategyDispatcher {

    private final List<ContentModerationStrategy> strategies;

    @Autowired
    public ModerationStrategyDispatcher(List<ContentModerationStrategy> strategies) {
        this.strategies = strategies;
        log.info("已加载 {} 个内容审核策略: {}", strategies.size(),
                strategies.stream().map(s -> s.getClass().getSimpleName()).toList());
    }

    /**
     * 根据内容类型分发到对应策略进行审核评分
     *
     * @param contentType 内容类型（"text"、"image" 等）
     * @param content     待审核内容
     * @param user        发布者
     * @return 风险评分结果
     * @throws UnsupportedContentTypeException 无匹配策略时抛出
     */
    public RiskScoreResult dispatch(String contentType, String content, User user) {
        for (ContentModerationStrategy strategy : strategies) {
            if (strategy.supports(contentType)) {
                log.debug("内容类型 [{}] 路由到策略: {}", contentType, strategy.getClass().getSimpleName());
                return strategy.evaluate(content, user);
            }
        }
        throw new UnsupportedContentTypeException(contentType);
    }

    /**
     * 检查是否支持指定的内容类型
     */
    public boolean supportsContentType(String contentType) {
        return strategies.stream().anyMatch(s -> s.supports(contentType));
    }

    /**
     * 不支持的内容类型异常
     */
    public static class UnsupportedContentTypeException extends RuntimeException {
        public UnsupportedContentTypeException(String contentType) {
            super("不支持的内容类型: " + contentType);
        }
    }
}
