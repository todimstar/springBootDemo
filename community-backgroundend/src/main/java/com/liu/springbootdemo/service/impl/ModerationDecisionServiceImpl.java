package com.liu.springbootdemo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.springbootdemo.POJO.dto.ModerationDecisionResult;
import com.liu.springbootdemo.POJO.dto.RiskScoreResult;
import com.liu.springbootdemo.POJO.entity.ModerationQueue;
import com.liu.springbootdemo.POJO.entity.ModerationThresholdConfig;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.common.enums.ModerationDecision;
import com.liu.springbootdemo.mapper.ModerationQueueMapper;
import com.liu.springbootdemo.mapper.ModerationThresholdConfigMapper;
import com.liu.springbootdemo.service.ModerationDecisionService;
import com.liu.springbootdemo.service.ModerationAuditLogService;
import com.liu.springbootdemo.service.RiskScoringService;
import com.liu.springbootdemo.service.strategy.ModerationStrategyDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 审核决策服务实现
 *
 * 决策逻辑：
 * - score < autoApproveMax → 自动放行（status=2 PUBLISHED）
 * - autoApproveMax ≤ score < autoRejectMin → 送人审（status=1 PENDING_REVIEW），写入审核队列
 * - score ≥ autoRejectMin → 自动拦截（status=3 REJECTED）
 */
@Service
@Slf4j
public class ModerationDecisionServiceImpl implements ModerationDecisionService {

    private static final String THRESHOLD_CACHE_KEY = "moderation:threshold:";
    private static final long THRESHOLD_CACHE_TTL_MINUTES = 10;

    private static final int DEFAULT_AUTO_APPROVE_MAX = 30;
    private static final int DEFAULT_AUTO_REJECT_MIN = 70;

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private ModerationThresholdConfigMapper thresholdConfigMapper;

    @Autowired
    private ModerationQueueMapper moderationQueueMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModerationAuditLogService auditLogService;

    @Autowired
    private ModerationStrategyDispatcher strategyDispatcher;

    @Override
    public ModerationDecisionResult decide(String text, User user, Long targetId, String targetType) {
        // 1. 风险评分（含用户画像加权）
        RiskScoreResult scoreResult = riskScoringService.evaluateWithUserProfile(text, user);
        int riskScore = scoreResult.getRiskScore();

        // 2. 加载阈值配置
        int autoApproveMax = loadThreshold("auto_approve_max", DEFAULT_AUTO_APPROVE_MAX);
        int autoRejectMin = loadThreshold("auto_reject_min", DEFAULT_AUTO_REJECT_MIN);

        // 3. 根据阈值做决策
        ModerationDecision decision;
        if (riskScore < autoApproveMax) {
            decision = ModerationDecision.AUTO_APPROVE;
        } else if (riskScore >= autoRejectMin) {
            decision = ModerationDecision.AUTO_REJECT;
        } else {
            decision = ModerationDecision.PENDING_REVIEW;
        }

        // 4. 灰区内容写入审核队列
        if (decision == ModerationDecision.PENDING_REVIEW) {
            enqueueForReview(targetId, targetType, user.getId(), text, riskScore, scoreResult);
        }

        log.info("审核决策完成: targetId={}, targetType={}, riskScore={}, decision={}, postStatus={}",
                targetId, targetType, riskScore, decision.getCode(), decision.getPostStatus());

        // 5. 写入审计日志（自动审核，操作人为 SYSTEM）
        auditLogService.logAutoAction(targetId, targetType, decision.getCode(),
                serializeHitRules(scoreResult), riskScore);

        return new ModerationDecisionResult(riskScore, decision, decision.getPostStatus(), scoreResult);
    }

    @Override
    public ModerationDecisionResult decide(String content, String contentType, User user, Long targetId, String targetType) {
        // 1. 通过策略分发器按内容类型进行风险评分
        RiskScoreResult scoreResult = strategyDispatcher.dispatch(contentType, content, user);
        int riskScore = scoreResult.getRiskScore();

        // 2. 加载阈值配置
        int autoApproveMax = loadThreshold("auto_approve_max", DEFAULT_AUTO_APPROVE_MAX);
        int autoRejectMin = loadThreshold("auto_reject_min", DEFAULT_AUTO_REJECT_MIN);

        // 3. 根据阈值做决策
        ModerationDecision decision;
        if (riskScore < autoApproveMax) {
            decision = ModerationDecision.AUTO_APPROVE;
        } else if (riskScore >= autoRejectMin) {
            decision = ModerationDecision.AUTO_REJECT;
        } else {
            decision = ModerationDecision.PENDING_REVIEW;
        }

        // 4. 灰区内容写入审核队列
        if (decision == ModerationDecision.PENDING_REVIEW) {
            enqueueForReview(targetId, targetType, user.getId(), content, riskScore, scoreResult);
        }

        log.info("审核决策完成: targetId={}, targetType={}, contentType={}, riskScore={}, decision={}",
                targetId, targetType, contentType, riskScore, decision.getCode());

        // 5. 写入审计日志
        auditLogService.logAutoAction(targetId, targetType, decision.getCode(),
                serializeHitRules(scoreResult), riskScore);

        return new ModerationDecisionResult(riskScore, decision, decision.getPostStatus(), scoreResult);
    }

    /**
     * 加载阈值配置，优先从 Redis 缓存读取
     */
    private int loadThreshold(String configKey, int defaultValue) {
        String cacheKey = THRESHOLD_CACHE_KEY + configKey;

        // 尝试从缓存读取
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof Integer) {
                return (Integer) cached;
            }
            if (cached instanceof Number) {
                return ((Number) cached).intValue();
            }
        } catch (Exception e) {
            log.warn("从 Redis 读取阈值缓存失败, key={}", configKey, e);
        }

        // 从数据库加载
        try {
            ModerationThresholdConfig config = thresholdConfigMapper.findByKey(configKey);
            if (config != null) {
                int value = config.getConfigValue();
                // 写入缓存
                try {
                    redisTemplate.opsForValue().set(cacheKey, value, THRESHOLD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.warn("写入阈值缓存到 Redis 失败, key={}", configKey, e);
                }
                return value;
            }
        } catch (Exception e) {
            log.error("从数据库加载阈值配置失败, key={}", configKey, e);
        }

        log.warn("使用默认阈值: {}={}", configKey, defaultValue);
        return defaultValue;
    }

    /**
     * 将灰区内容写入审核队列
     */
    private void enqueueForReview(Long targetId, String targetType, Long authorId,
                                   String text, int riskScore, RiskScoreResult scoreResult) {
        try {
            ModerationQueue queue = new ModerationQueue();
            queue.setTargetId(targetId);
            queue.setTargetType(targetType);
            queue.setAuthorId(authorId);
            queue.setContentSummary(text.length() > 500 ? text.substring(0, 500) : text);
            queue.setRiskScore(riskScore);
            queue.setHitRules(serializeHitRules(scoreResult));
            queue.setStatus("pending");

            moderationQueueMapper.insert(queue);
            log.info("内容已加入审核队列: targetId={}, targetType={}, riskScore={}", targetId, targetType, riskScore);
        } catch (Exception e) {
            log.error("写入审核队列失败: targetId={}, targetType={}", targetId, targetType, e);
        }
    }

    /**
     * 序列化命中规则为 JSON
     */
    private String serializeHitRules(RiskScoreResult scoreResult) {
        try {
            return objectMapper.writeValueAsString(scoreResult.getHitResults());
        } catch (JsonProcessingException e) {
            log.warn("序列化命中规则失败", e);
            return "[]";
        }
    }
}
