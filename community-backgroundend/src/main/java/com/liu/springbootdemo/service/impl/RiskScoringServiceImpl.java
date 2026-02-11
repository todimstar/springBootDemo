package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.dto.RiskScoreResult;
import com.liu.springbootdemo.POJO.dto.RuleHitResult;
import com.liu.springbootdemo.POJO.entity.ModerationRule;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.common.utils.TextNormalizer;
import com.liu.springbootdemo.mapper.ModerationRuleMapper;
import com.liu.springbootdemo.service.RiskScoringService;
import com.liu.springbootdemo.service.UserRiskWeightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 内容风险评分引擎实现
 * <p>
 * 规则按责任链顺序执行：关键词匹配 → 正则匹配 → 黑名单检查
 * 累加所有命中规则的权重分，封顶 100
 */
@Service
@Slf4j
public class RiskScoringServiceImpl implements RiskScoringService {

    private static final String RULES_CACHE_KEY = "moderation:rules:enabled";
    private static final long RULES_CACHE_TTL_MINUTES = 10;

    @Autowired
    private ModerationRuleMapper moderationRuleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRiskWeightService userRiskWeightService;

    @Override
    public RiskScoreResult evaluate(String text) {
        if (text == null || text.isBlank()) {
            return RiskScoreResult.safe();
        }

        // 文本预处理
        String normalizedText = TextNormalizer.normalize(text);

        // 加载规则（优先从缓存读取）
        List<ModerationRule> rules = loadEnabledRules();

        // 依次执行规则匹配
        List<RuleHitResult> hitResults = new ArrayList<>();
        int totalScore = 0;

        for (ModerationRule rule : rules) {
            RuleHitResult hit = matchRule(rule, normalizedText);
            if (hit != null) {
                hitResults.add(hit);
                totalScore += hit.getWeightScore();
            }
        }

        // 封顶 100
        int finalScore = Math.min(totalScore, 100);

        return new RiskScoreResult(finalScore, hitResults);
    }

    @Override
    public RiskScoreResult evaluateWithUserProfile(String text, User user) {
        // 先进行纯文本评分
        RiskScoreResult baseResult = evaluate(text);

        // 计算用户画像加权系数
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        // 最终评分 = 文本基础分 × 加权系数，封顶 100
        int weightedScore = (int) Math.min(baseResult.getRiskScore() * multiplier, 100);

        return new RiskScoreResult(weightedScore, baseResult.getHitResults());
    }

    /**
     * 加载启用的规则，优先从 Redis 缓存读取
     */
    @SuppressWarnings("unchecked")
    private List<ModerationRule> loadEnabledRules() {
        try {
            Object cached = redisTemplate.opsForValue().get(RULES_CACHE_KEY);
            if (cached instanceof List) {
                return (List<ModerationRule>) cached;
            }
        } catch (Exception e) {
            log.warn("从 Redis 读取规则缓存失败，回退到数据库查询", e);
        }

        // 缓存未命中，从数据库加载
        List<ModerationRule> rules = moderationRuleMapper.findAllEnabled();

        // 写入缓存
        try {
            redisTemplate.opsForValue().set(RULES_CACHE_KEY, rules, RULES_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写入规则缓存到 Redis 失败", e);
        }

        return rules;
    }

    /**
     * 按规则类型分发匹配逻辑
     */
    private RuleHitResult matchRule(ModerationRule rule, String normalizedText) {
        return switch (rule.getRuleType()) {
            case "keyword" -> matchKeyword(rule, normalizedText);
            case "regex" -> matchRegex(rule, normalizedText);
            case "blacklist" -> matchBlacklist(rule, normalizedText);
            default -> {
                log.warn("未知的规则类型: {}, ruleId: {}", rule.getRuleType(), rule.getId());
                yield null;
            }
        };
    }

    /**
     * 关键词匹配：文本中是否包含该关键词
     */
    private RuleHitResult matchKeyword(ModerationRule rule, String normalizedText) {
        String keyword = rule.getPattern().toLowerCase();
        if (normalizedText.contains(keyword)) {
            return new RuleHitResult(rule.getId(), rule.getRuleType(), keyword, rule.getWeightScore());
        }
        return null;
    }

    /**
     * 正则匹配：文本是否匹配该正则表达式
     */
    private RuleHitResult matchRegex(ModerationRule rule, String normalizedText) {
        try {
            Pattern pattern = Pattern.compile(rule.getPattern(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(normalizedText);
            if (matcher.find()) {
                return new RuleHitResult(rule.getId(), rule.getRuleType(), matcher.group(), rule.getWeightScore());
            }
        } catch (Exception e) {
            log.error("正则表达式编译失败, ruleId: {}, pattern: {}", rule.getId(), rule.getPattern(), e);
        }
        return null;
    }

    /**
     * 黑名单匹配：文本中是否包含黑名单项（URL/域名/敏感短语）
     */
    private RuleHitResult matchBlacklist(ModerationRule rule, String normalizedText) {
        String blacklistItem = rule.getPattern().toLowerCase();
        if (normalizedText.contains(blacklistItem)) {
            return new RuleHitResult(rule.getId(), rule.getRuleType(), blacklistItem, rule.getWeightScore());
        }
        return null;
    }
}
