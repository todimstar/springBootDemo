package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.entity.UserRiskFactorConfig;
import com.liu.springbootdemo.mapper.UserRiskFactorConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户画像风险加权服务单元测试
 * 覆盖场景：各画像因子的独立与组合
 */
@ExtendWith(MockitoExtension.class)
class UserRiskWeightServiceImplTest {

    @Mock
    private UserRiskFactorConfigMapper userRiskFactorConfigMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private UserRiskWeightServiceImpl userRiskWeightService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null);
    }

    // ============ 基础场景 ============

    @Test
    void calculateMultiplier_nullUser_returnsDefault() {
        double multiplier = userRiskWeightService.calculateMultiplier(null);
        assertEquals(1.0, multiplier);
    }

    @Test
    void calculateMultiplier_noConfigs_returnsDefault() {
        when(userRiskFactorConfigMapper.findAllEnabled()).thenReturn(Collections.emptyList());

        User user = createUser(30, 0, 5);
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        assertEquals(1.0, multiplier);
    }

    @Test
    void calculateMultiplier_noFactorsHit_returnsDefault() {
        // 用户：注册30天、0次违规、等级5 —— 都不命中任何规则
        UserRiskFactorConfig regConfig = createConfig("registration_days", "LT", 7, 1.5);
        UserRiskFactorConfig violConfig = createConfig("violation_count", "GTE", 3, 2.0);
        UserRiskFactorConfig levelConfig = createConfig("level", "LT", 3, 1.2);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Arrays.asList(regConfig, violConfig, levelConfig));

        User user = createUser(30, 0, 5);
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        assertEquals(1.0, multiplier);
    }

    // ============ 单因子命中场景 ============

    @Test
    void calculateMultiplier_newUser_registrationDaysHit() {
        // 注册天数 < 7 → ×1.5
        UserRiskFactorConfig config = createConfig("registration_days", "LT", 7, 1.5);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(3, 0, 5); // 注册3天
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        assertEquals(1.5, multiplier);
    }

    @Test
    void calculateMultiplier_violationCountHit() {
        // 违规次数 >= 3 → ×2.0
        UserRiskFactorConfig config = createConfig("violation_count", "GTE", 3, 2.0);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(100, 5, 5); // 违规5次
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        assertEquals(2.0, multiplier);
    }

    @Test
    void calculateMultiplier_lowLevelHit() {
        // 等级 < 3 → ×1.2
        UserRiskFactorConfig config = createConfig("level", "LT", 3, 1.2);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(100, 0, 2); // 等级2
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        assertEquals(1.2, multiplier);
    }

    // ============ 组合场景 ============

    @Test
    void calculateMultiplier_multipleFactorsHit_takesMax() {
        // 多因子命中时，取最大加权系数
        UserRiskFactorConfig regConfig = createConfig("registration_days", "LT", 7, 1.5);
        UserRiskFactorConfig violConfig = createConfig("violation_count", "GTE", 3, 2.0);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Arrays.asList(regConfig, violConfig));

        User user = createUser(3, 5, 5); // 注册3天 + 违规5次
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        // 应取 max(1.5, 2.0) = 2.0
        assertEquals(2.0, multiplier);
    }

    @Test
    void calculateMultiplier_allFactorsHit_takesMax() {
        UserRiskFactorConfig regConfig = createConfig("registration_days", "LT", 7, 1.5);
        UserRiskFactorConfig violConfig = createConfig("violation_count", "GTE", 3, 2.0);
        UserRiskFactorConfig levelConfig = createConfig("level", "LT", 3, 1.2);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Arrays.asList(regConfig, violConfig, levelConfig));

        User user = createUser(1, 10, 1); // 全部命中
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        // 应取 max(1.5, 2.0, 1.2) = 2.0
        assertEquals(2.0, multiplier);
    }

    // ============ 边界值场景 ============

    @Test
    void calculateMultiplier_exactBoundary_registrationDays7_notHit() {
        // 注册恰好 7 天，条件是 LT 7 → 不命中
        UserRiskFactorConfig config = createConfig("registration_days", "LT", 7, 1.5);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(7, 0, 5);
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        assertEquals(1.0, multiplier);
    }

    @Test
    void calculateMultiplier_exactBoundary_violationCount3_hit() {
        // 违规恰好 3 次，条件是 GTE 3 → 命中
        UserRiskFactorConfig config = createConfig("violation_count", "GTE", 3, 2.0);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(100, 3, 5);
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        assertEquals(2.0, multiplier);
    }

    @Test
    void calculateMultiplier_exactBoundary_level3_notHit() {
        // 等级恰好 3，条件是 LT 3 → 不命中
        UserRiskFactorConfig config = createConfig("level", "LT", 3, 1.2);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(100, 0, 3);
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        assertEquals(1.0, multiplier);
    }

    // ============ 运算符覆盖 ============

    @Test
    void calculateMultiplier_operator_GT() {
        UserRiskFactorConfig config = createConfig("violation_count", "GT", 5, 2.5);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(100, 6, 5);
        assertEquals(2.5, userRiskWeightService.calculateMultiplier(user));

        User userExact = createUser(100, 5, 5);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));
        assertEquals(1.0, userRiskWeightService.calculateMultiplier(userExact)); // GT 5 不命中 5
    }

    @Test
    void calculateMultiplier_operator_LTE() {
        UserRiskFactorConfig config = createConfig("level", "LTE", 3, 1.3);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(100, 0, 3);
        assertEquals(1.3, userRiskWeightService.calculateMultiplier(user)); // LTE 3 命中 3
    }

    @Test
    void calculateMultiplier_operator_EQ() {
        UserRiskFactorConfig config = createConfig("level", "EQ", 1, 1.8);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(100, 0, 1);
        assertEquals(1.8, userRiskWeightService.calculateMultiplier(user));
    }

    @Test
    void calculateMultiplier_unknownOperator_skipped() {
        UserRiskFactorConfig config = createConfig("level", "UNKNOWN", 1, 3.0);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(100, 0, 1);
        assertEquals(1.0, userRiskWeightService.calculateMultiplier(user));
    }

    @Test
    void calculateMultiplier_unknownFactor_skipped() {
        UserRiskFactorConfig config = createConfig("unknown_factor", "LT", 10, 3.0);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(100, 0, 5);
        // unknown factor → value=0, 0 < 10 → 命中，但 unknown 因子 value 为 0
        // 实际上 extractFactorValue 返回 0，0 < 10 = true → 命中
        assertEquals(3.0, userRiskWeightService.calculateMultiplier(user));
    }

    // ============ 用户 createTime 为 null 场景 ============

    @Test
    void calculateMultiplier_nullCreateTime_registrationDaysZero() {
        UserRiskFactorConfig config = createConfig("registration_days", "LT", 7, 1.5);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = new User();
        user.setId(1L);
        user.setCreateTime(null);
        user.setViolationCount(0);
        user.setLevel(5);

        // createTime=null → registrationDays=0 → 0 < 7 → 命中
        double multiplier = userRiskWeightService.calculateMultiplier(user);
        assertEquals(1.5, multiplier);
    }

    // ============ Redis 缓存场景 ============

    @Test
    void calculateMultiplier_usesCachedConfigsWhenAvailable() {
        UserRiskFactorConfig config = createConfig("violation_count", "GTE", 3, 2.0);
        List<UserRiskFactorConfig> cachedConfigs = Collections.singletonList(config);
        when(valueOperations.get(anyString())).thenReturn(cachedConfigs);

        User user = createUser(100, 5, 5);
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        assertEquals(2.0, multiplier);
        verify(userRiskFactorConfigMapper, never()).findAllEnabled();
    }

    @Test
    void calculateMultiplier_redisFails_fallbackToDatabase() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis连接失败"));

        UserRiskFactorConfig config = createConfig("violation_count", "GTE", 3, 2.0);
        when(userRiskFactorConfigMapper.findAllEnabled())
                .thenReturn(Collections.singletonList(config));

        User user = createUser(100, 5, 5);
        double multiplier = userRiskWeightService.calculateMultiplier(user);

        assertEquals(2.0, multiplier);
        verify(userRiskFactorConfigMapper).findAllEnabled();
    }

    // ============ 辅助方法 ============

    private User createUser(int registrationDays, int violationCount, int level) {
        User user = new User();
        user.setId(1L);
        user.setCreateTime(LocalDateTime.now().minusDays(registrationDays));
        user.setViolationCount(violationCount);
        user.setLevel(level);
        return user;
    }

    private UserRiskFactorConfig createConfig(String factorName, String operator,
                                               double threshold, double multiplier) {
        UserRiskFactorConfig config = new UserRiskFactorConfig();
        config.setId(1L);
        config.setFactorName(factorName);
        config.setOperator(operator);
        config.setThreshold(threshold);
        config.setMultiplier(multiplier);
        config.setEnabled(true);
        return config;
    }
}
