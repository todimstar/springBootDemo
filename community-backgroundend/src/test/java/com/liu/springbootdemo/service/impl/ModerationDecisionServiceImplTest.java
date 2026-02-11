package com.liu.springbootdemo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.springbootdemo.POJO.dto.ModerationDecisionResult;
import com.liu.springbootdemo.POJO.dto.RiskScoreResult;
import com.liu.springbootdemo.POJO.dto.RuleHitResult;
import com.liu.springbootdemo.POJO.entity.ModerationQueue;
import com.liu.springbootdemo.POJO.entity.ModerationThresholdConfig;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.common.enums.ModerationDecision;
import com.liu.springbootdemo.common.enums.PostStatus;
import com.liu.springbootdemo.mapper.ModerationQueueMapper;
import com.liu.springbootdemo.mapper.ModerationThresholdConfigMapper;
import com.liu.springbootdemo.service.ModerationAuditLogService;
import com.liu.springbootdemo.service.RiskScoringService;
import com.liu.springbootdemo.service.strategy.ModerationStrategyDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 审核决策服务单元测试
 * 覆盖场景：自动放行、送人审、自动拦截、边界值、阈值配置、审核队列
 */
@ExtendWith(MockitoExtension.class)
class ModerationDecisionServiceImplTest {

    @Mock
    private RiskScoringService riskScoringService;

    @Mock
    private ModerationThresholdConfigMapper thresholdConfigMapper;

    @Mock
    private ModerationQueueMapper moderationQueueMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ModerationAuditLogService auditLogService;

    @Mock
    private ModerationStrategyDispatcher strategyDispatcher;

    @InjectMocks
    private ModerationDecisionServiceImpl moderationDecisionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    // ============ 自动放行场景（score < 30） ============

    @Test
    void decide_scoreBelowApproveThreshold_autoApprove() {
        setupThresholdConfig(30, 70);
        setupRiskScore(10);

        ModerationDecisionResult result = moderationDecisionService.decide("正常内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.AUTO_APPROVE, result.getDecision());
        assertEquals(PostStatus.PUBLISHED.getStatus(), result.getPostStatus());
        assertEquals(10, result.getRiskScore());
        verify(moderationQueueMapper, never()).insert(any());
    }

    @Test
    void decide_scoreZero_autoApprove() {
        setupThresholdConfig(30, 70);
        setupRiskScore(0);

        ModerationDecisionResult result = moderationDecisionService.decide("安全内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.AUTO_APPROVE, result.getDecision());
        assertEquals(PostStatus.PUBLISHED.getStatus(), result.getPostStatus());
        verify(moderationQueueMapper, never()).insert(any());
    }

    @Test
    void decide_scoreJustBelowApproveMax_autoApprove() {
        setupThresholdConfig(30, 70);
        setupRiskScore(29);

        ModerationDecisionResult result = moderationDecisionService.decide("临界内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.AUTO_APPROVE, result.getDecision());
        assertEquals(PostStatus.PUBLISHED.getStatus(), result.getPostStatus());
        verify(moderationQueueMapper, never()).insert(any());
    }

    // ============ 送人审场景（30 ≤ score < 70） ============

    @Test
    void decide_scoreAtApproveMax_pendingReview() {
        setupThresholdConfig(30, 70);
        setupRiskScore(30);

        ModerationDecisionResult result = moderationDecisionService.decide("灰区内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.PENDING_REVIEW, result.getDecision());
        assertEquals(PostStatus.PENDING_REVIEW.getStatus(), result.getPostStatus());
        assertEquals(30, result.getRiskScore());
        verify(moderationQueueMapper).insert(any(ModerationQueue.class));
    }

    @Test
    void decide_scoreInMiddleRange_pendingReview() {
        setupThresholdConfig(30, 70);
        setupRiskScore(50);

        ModerationDecisionResult result = moderationDecisionService.decide("可疑内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.PENDING_REVIEW, result.getDecision());
        assertEquals(PostStatus.PENDING_REVIEW.getStatus(), result.getPostStatus());
        verify(moderationQueueMapper).insert(any(ModerationQueue.class));
    }

    @Test
    void decide_scoreJustBelowRejectMin_pendingReview() {
        setupThresholdConfig(30, 70);
        setupRiskScore(69);

        ModerationDecisionResult result = moderationDecisionService.decide("临界内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.PENDING_REVIEW, result.getDecision());
        assertEquals(PostStatus.PENDING_REVIEW.getStatus(), result.getPostStatus());
        verify(moderationQueueMapper).insert(any(ModerationQueue.class));
    }

    // ============ 自动拦截场景（score ≥ 70） ============

    @Test
    void decide_scoreAtRejectMin_autoReject() {
        setupThresholdConfig(30, 70);
        setupRiskScore(70);

        ModerationDecisionResult result = moderationDecisionService.decide("违规内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.AUTO_REJECT, result.getDecision());
        assertEquals(PostStatus.REJECTED.getStatus(), result.getPostStatus());
        assertEquals(70, result.getRiskScore());
        verify(moderationQueueMapper, never()).insert(any());
    }

    @Test
    void decide_scoreMax100_autoReject() {
        setupThresholdConfig(30, 70);
        setupRiskScore(100);

        ModerationDecisionResult result = moderationDecisionService.decide("严重违规", testUser, 100L, "post");

        assertEquals(ModerationDecision.AUTO_REJECT, result.getDecision());
        assertEquals(PostStatus.REJECTED.getStatus(), result.getPostStatus());
        verify(moderationQueueMapper, never()).insert(any());
    }

    // ============ 审核队列写入验证 ============

    @Test
    void decide_pendingReview_writesToQueue() {
        setupThresholdConfig(30, 70);
        List<RuleHitResult> hits = Collections.singletonList(
                new RuleHitResult(1L, "keyword", "敏感词", 50));
        when(riskScoringService.evaluateWithUserProfile(anyString(), any(User.class)))
                .thenReturn(new RiskScoreResult(50, hits));

        moderationDecisionService.decide("包含敏感词的内容", testUser, 200L, "post");

        ArgumentCaptor<ModerationQueue> captor = ArgumentCaptor.forClass(ModerationQueue.class);
        verify(moderationQueueMapper).insert(captor.capture());

        ModerationQueue queue = captor.getValue();
        assertEquals(200L, queue.getTargetId());
        assertEquals("post", queue.getTargetType());
        assertEquals(1L, queue.getAuthorId());
        assertEquals(50, queue.getRiskScore());
        assertEquals("pending", queue.getStatus());
        assertNotNull(queue.getContentSummary());
        assertNotNull(queue.getHitRules());
    }

    @Test
    void decide_pendingReview_contentSummaryTruncated() {
        setupThresholdConfig(30, 70);
        setupRiskScore(50);

        String longText = "a".repeat(600);
        moderationDecisionService.decide(longText, testUser, 100L, "post");

        ArgumentCaptor<ModerationQueue> captor = ArgumentCaptor.forClass(ModerationQueue.class);
        verify(moderationQueueMapper).insert(captor.capture());
        assertEquals(500, captor.getValue().getContentSummary().length());
    }

    @Test
    void decide_pendingReview_commentType() {
        setupThresholdConfig(30, 70);
        setupRiskScore(40);

        ModerationDecisionResult result = moderationDecisionService.decide("可疑评论", testUser, 300L, "comment");

        assertEquals(ModerationDecision.PENDING_REVIEW, result.getDecision());

        ArgumentCaptor<ModerationQueue> captor = ArgumentCaptor.forClass(ModerationQueue.class);
        verify(moderationQueueMapper).insert(captor.capture());
        assertEquals("comment", captor.getValue().getTargetType());
        assertEquals(300L, captor.getValue().getTargetId());
    }

    // ============ 阈值配置场景 ============

    @Test
    void decide_customThresholds_appliedCorrectly() {
        // 自定义阈值：score < 20 放行，score >= 80 拦截
        setupThresholdConfig(20, 80);
        setupRiskScore(25);

        ModerationDecisionResult result = moderationDecisionService.decide("测试内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.PENDING_REVIEW, result.getDecision());
    }

    @Test
    void decide_noDbConfig_usesDefaults() {
        // 数据库返回 null，使用默认值
        when(thresholdConfigMapper.findByKey(anyString())).thenReturn(null);
        setupRiskScore(10);

        ModerationDecisionResult result = moderationDecisionService.decide("正常内容", testUser, 100L, "post");

        // 默认 auto_approve_max=30, score=10 < 30 → 自动放行
        assertEquals(ModerationDecision.AUTO_APPROVE, result.getDecision());
    }

    @Test
    void decide_thresholdFromCache_usesCache() {
        when(valueOperations.get("moderation:threshold:auto_approve_max")).thenReturn(25);
        when(valueOperations.get("moderation:threshold:auto_reject_min")).thenReturn(75);
        setupRiskScore(26);

        ModerationDecisionResult result = moderationDecisionService.decide("测试内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.PENDING_REVIEW, result.getDecision());
        verify(thresholdConfigMapper, never()).findByKey(anyString());
    }

    @Test
    void decide_cacheFails_fallbackToDb() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis不可用"));
        setupThresholdConfig(30, 70);
        setupRiskScore(80);

        ModerationDecisionResult result = moderationDecisionService.decide("违规内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.AUTO_REJECT, result.getDecision());
    }

    // ============ 结果完整性验证 ============

    @Test
    void decide_resultContainsScoreDetails() {
        setupThresholdConfig(30, 70);
        List<RuleHitResult> hits = List.of(
                new RuleHitResult(1L, "keyword", "赌博", 40),
                new RuleHitResult(2L, "regex", "v信:abc", 30));
        when(riskScoringService.evaluateWithUserProfile(anyString(), any(User.class)))
                .thenReturn(new RiskScoreResult(70, hits));

        ModerationDecisionResult result = moderationDecisionService.decide("赌博 v信:abc", testUser, 100L, "post");

        assertNotNull(result.getScoreResult());
        assertEquals(70, result.getScoreResult().getRiskScore());
        assertEquals(2, result.getScoreResult().getHitResults().size());
    }

    // ============ 审核队列写入失败场景 ============

    @Test
    void decide_queueInsertFails_doesNotThrow() {
        setupThresholdConfig(30, 70);
        setupRiskScore(50);
        doThrow(new RuntimeException("DB error")).when(moderationQueueMapper).insert(any());

        // 不应该抛出异常，队列写入失败不影响决策结果
        ModerationDecisionResult result = moderationDecisionService.decide("灰区内容", testUser, 100L, "post");

        assertEquals(ModerationDecision.PENDING_REVIEW, result.getDecision());
    }

    // ============ 辅助方法 ============

    private void setupThresholdConfig(int autoApproveMax, int autoRejectMin) {
        ModerationThresholdConfig approveConfig = new ModerationThresholdConfig();
        approveConfig.setConfigKey("auto_approve_max");
        approveConfig.setConfigValue(autoApproveMax);

        ModerationThresholdConfig rejectConfig = new ModerationThresholdConfig();
        rejectConfig.setConfigKey("auto_reject_min");
        rejectConfig.setConfigValue(autoRejectMin);

        lenient().when(thresholdConfigMapper.findByKey("auto_approve_max")).thenReturn(approveConfig);
        lenient().when(thresholdConfigMapper.findByKey("auto_reject_min")).thenReturn(rejectConfig);
    }

    private void setupRiskScore(int score) {
        when(riskScoringService.evaluateWithUserProfile(anyString(), any(User.class)))
                .thenReturn(new RiskScoreResult(score, new ArrayList<>()));
    }
}
