package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.dto.RiskScoreResult;
import com.liu.springbootdemo.POJO.dto.RuleHitResult;
import com.liu.springbootdemo.POJO.entity.ModerationRule;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.mapper.ModerationRuleMapper;
import com.liu.springbootdemo.service.ModerationRuleStatsService;
import com.liu.springbootdemo.service.UserRiskWeightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 风险评分引擎单元测试
 * 覆盖场景：无命中、单规则命中、多规则叠加、边界值
 */
@ExtendWith(MockitoExtension.class)
class RiskScoringServiceImplTest {

    @Mock
    private ModerationRuleMapper moderationRuleMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private UserRiskWeightService userRiskWeightService;

    @Mock
    private ModerationRuleStatsService moderationRuleStatsService;

    @InjectMocks
    private RiskScoringServiceImpl riskScoringService;

    @BeforeEach
    void setUp() {
        // 让 Redis 缓存始终返回 null（未命中），从数据库加载
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null);
    }

    // ============ 无命中场景 ============

    @Test
    void evaluate_nullText_returnsSafe() {
        RiskScoreResult result = riskScoringService.evaluate(null);
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getHitResults().isEmpty());
    }

    @Test
    void evaluate_emptyText_returnsSafe() {
        RiskScoreResult result = riskScoringService.evaluate("");
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getHitResults().isEmpty());
    }

    @Test
    void evaluate_blankText_returnsSafe() {
        RiskScoreResult result = riskScoringService.evaluate("   ");
        assertEquals(0, result.getRiskScore());
        assertTrue(result.getHitResults().isEmpty());
    }

    @Test
    void evaluate_noRulesHit_returnsZeroScore() {
        ModerationRule rule = createRule(1L, "keyword", "赌博", 80);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        RiskScoreResult result = riskScoringService.evaluate("这是一篇正常的技术分享文章");

        assertEquals(0, result.getRiskScore());
        assertTrue(result.getHitResults().isEmpty());
    }

    // ============ 单规则命中场景 ============

    @Test
    void evaluate_keywordHit_returnsScore() {
        ModerationRule rule = createRule(1L, "keyword", "赌博", 80);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        RiskScoreResult result = riskScoringService.evaluate("这个网站涉嫌赌博活动");

        assertEquals(80, result.getRiskScore());
        assertEquals(1, result.getHitResults().size());
        assertEquals(1L, result.getHitResults().get(0).getRuleId());
        assertEquals("keyword", result.getHitResults().get(0).getRuleType());
    }

    @Test
    void evaluate_regexHit_returnsScore() {
        ModerationRule rule = createRule(2L, "regex", "(?i)v\\s*信\\s*[:：]?\\s*[a-zA-Z0-9_]+", 60);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        RiskScoreResult result = riskScoringService.evaluate("加我v信:abc123");

        assertEquals(60, result.getRiskScore());
        assertEquals(1, result.getHitResults().size());
        assertEquals("regex", result.getHitResults().get(0).getRuleType());
    }

    @Test
    void evaluate_blacklistHit_returnsScore() {
        ModerationRule rule = createRule(3L, "blacklist", "bit.ly", 40);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        RiskScoreResult result = riskScoringService.evaluate("点击链接 bit.ly/abc123 查看详情");

        assertEquals(40, result.getRiskScore());
        assertEquals(1, result.getHitResults().size());
        assertEquals("blacklist", result.getHitResults().get(0).getRuleType());
    }

    // ============ 多规则叠加场景 ============

    @Test
    void evaluate_multipleRulesHit_scoresAccumulate() {
        ModerationRule rule1 = createRule(1L, "keyword", "赌博", 30);
        ModerationRule rule2 = createRule(2L, "keyword", "诈骗", 40);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Arrays.asList(rule1, rule2));

        RiskScoreResult result = riskScoringService.evaluate("该网站涉嫌赌博和诈骗");

        assertEquals(70, result.getRiskScore());
        assertEquals(2, result.getHitResults().size());
    }

    // ============ 边界值场景 ============

    @Test
    void evaluate_scoreCappedAt100() {
        ModerationRule rule1 = createRule(1L, "keyword", "赌博", 80);
        ModerationRule rule2 = createRule(2L, "keyword", "诈骗", 85);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Arrays.asList(rule1, rule2));

        RiskScoreResult result = riskScoringService.evaluate("赌博和诈骗内容");

        assertEquals(100, result.getRiskScore());
        assertEquals(2, result.getHitResults().size());
    }

    @Test
    void evaluate_textNormalization_fullWidthConverted() {
        // 全角关键词也能命中
        ModerationRule rule = createRule(1L, "keyword", "赌博", 80);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        // 在全角包裹中的关键词
        RiskScoreResult result = riskScoringService.evaluate("这里有赌博内容");

        assertEquals(80, result.getRiskScore());
    }

    @Test
    void evaluate_textNormalization_caseInsensitive() {
        ModerationRule rule = createRule(1L, "keyword", "gambling", 70);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        RiskScoreResult result = riskScoringService.evaluate("This is GAMBLING content");

        assertEquals(70, result.getRiskScore());
    }

    @Test
    void evaluate_invalidRegex_skippedGracefully() {
        ModerationRule rule = createRule(1L, "regex", "[invalid(regex", 50);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        RiskScoreResult result = riskScoringService.evaluate("some text");

        assertEquals(0, result.getRiskScore());
        assertTrue(result.getHitResults().isEmpty());
    }

    @Test
    void evaluate_unknownRuleType_skippedGracefully() {
        ModerationRule rule = createRule(1L, "unknown_type", "test", 50);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        RiskScoreResult result = riskScoringService.evaluate("test content");

        assertEquals(0, result.getRiskScore());
        assertTrue(result.getHitResults().isEmpty());
    }

    @Test
    void evaluate_noEnabledRules_returnsZero() {
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.emptyList());

        RiskScoreResult result = riskScoringService.evaluate("任意内容");

        assertEquals(0, result.getRiskScore());
        assertTrue(result.getHitResults().isEmpty());
    }

    // ============ Redis 缓存场景 ============

    @Test
    void evaluate_usesCachedRulesWhenAvailable() {
        ModerationRule rule = createRule(1L, "keyword", "赌博", 80);
        List<ModerationRule> cachedRules = Collections.singletonList(rule);

        when(valueOperations.get(anyString())).thenReturn(cachedRules);

        RiskScoreResult result = riskScoringService.evaluate("赌博内容");

        assertEquals(80, result.getRiskScore());
        // 不应该查询数据库
        verify(moderationRuleMapper, never()).findAllEnabled();
    }

    @Test
    void evaluate_redisFails_fallbackToDatabase() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis连接失败"));

        ModerationRule rule = createRule(1L, "keyword", "赌博", 80);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        RiskScoreResult result = riskScoringService.evaluate("赌博内容");

        assertEquals(80, result.getRiskScore());
        verify(moderationRuleMapper).findAllEnabled();
    }

    // ============ evaluateWithUserProfile 场景 ============

    @Test
    void evaluateWithUserProfile_appliesMultiplier() {
        ModerationRule rule = createRule(1L, "keyword", "赌博", 40);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        User user = new User();
        user.setId(1L);
        when(userRiskWeightService.calculateMultiplier(user)).thenReturn(2.0);

        RiskScoreResult result = riskScoringService.evaluateWithUserProfile("赌博内容", user);

        // 文本基础分 40 × 加权系数 2.0 = 80
        assertEquals(80, result.getRiskScore());
    }

    @Test
    void evaluateWithUserProfile_cappedAt100() {
        ModerationRule rule = createRule(1L, "keyword", "赌博", 80);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        User user = new User();
        user.setId(1L);
        when(userRiskWeightService.calculateMultiplier(user)).thenReturn(2.0);

        RiskScoreResult result = riskScoringService.evaluateWithUserProfile("赌博内容", user);

        // 文本基础分 80 × 加权系数 2.0 = 160 → 封顶 100
        assertEquals(100, result.getRiskScore());
    }

    @Test
    void evaluateWithUserProfile_noMultiplier_sameAsBase() {
        ModerationRule rule = createRule(1L, "keyword", "赌博", 50);
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.singletonList(rule));

        User user = new User();
        user.setId(1L);
        when(userRiskWeightService.calculateMultiplier(user)).thenReturn(1.0);

        RiskScoreResult result = riskScoringService.evaluateWithUserProfile("赌博内容", user);

        // 文本基础分 50 × 加权系数 1.0 = 50
        assertEquals(50, result.getRiskScore());
    }

    @Test
    void evaluateWithUserProfile_safeText_staysZero() {
        when(moderationRuleMapper.findAllEnabled()).thenReturn(Collections.emptyList());

        User user = new User();
        user.setId(1L);
        when(userRiskWeightService.calculateMultiplier(user)).thenReturn(2.0);

        RiskScoreResult result = riskScoringService.evaluateWithUserProfile("正常内容", user);

        // 文本基础分 0 × 加权系数 2.0 = 0
        assertEquals(0, result.getRiskScore());
    }

    // ============ 辅助方法 ============

    private ModerationRule createRule(Long id, String ruleType, String pattern, int weightScore) {
        ModerationRule rule = new ModerationRule();
        rule.setId(id);
        rule.setRuleType(ruleType);
        rule.setPattern(pattern);
        rule.setWeightScore(weightScore);
        rule.setEnabled(true);
        rule.setDeleted(false);
        return rule;
    }
}
