package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.entity.UserRiskFactorConfig;
import com.liu.springbootdemo.mapper.UserRiskFactorConfigMapper;
import com.liu.springbootdemo.service.UserRiskWeightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户画像风险加权服务实现
 * <p>
 * 评估用户的注册天数、历史违规次数、账号等级等画像因子，
 * 取所有命中因子中最大的加权系数作为最终加权系数。
 */
@Service
@Slf4j
public class UserRiskWeightServiceImpl implements UserRiskWeightService {

    private static final String CONFIG_CACHE_KEY = "moderation:user_risk_factor:config";
    private static final long CONFIG_CACHE_TTL_MINUTES = 10;
    private static final double DEFAULT_MULTIPLIER = 1.0;

    @Autowired
    private UserRiskFactorConfigMapper userRiskFactorConfigMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public double calculateMultiplier(User user) {
        if (user == null) {
            return DEFAULT_MULTIPLIER;
        }

        List<UserRiskFactorConfig> configs = loadEnabledConfigs();
        if (configs.isEmpty()) {
            return DEFAULT_MULTIPLIER;
        }

        double maxMultiplier = DEFAULT_MULTIPLIER;

        for (UserRiskFactorConfig config : configs) {
            double factorValue = extractFactorValue(user, config.getFactorName());
            if (matchesCondition(factorValue, config.getOperator(), config.getThreshold())) {
                maxMultiplier = Math.max(maxMultiplier, config.getMultiplier());
                log.debug("用户[{}] 命中风险因子: {} {} {}, 加权系数: {}",
                        user.getId(), config.getFactorName(), config.getOperator(),
                        config.getThreshold(), config.getMultiplier());
            }
        }

        return maxMultiplier;
    }

    /**
     * 从用户信息中提取画像因子的实际值
     */
    private double extractFactorValue(User user, String factorName) {
        return switch (factorName) {
            case "registration_days" -> {
                if (user.getCreateTime() == null) {
                    yield 0;
                }
                yield ChronoUnit.DAYS.between(user.getCreateTime(), LocalDateTime.now());
            }
            case "violation_count" -> user.getViolationCount();
            case "level" -> user.getLevel();
            default -> {
                log.warn("未知的画像因子: {}", factorName);
                yield 0;
            }
        };
    }

    /**
     * 判断因子值是否满足配置的比较条件
     */
    private boolean matchesCondition(double factorValue, String operator, double threshold) {
        return switch (operator.toUpperCase()) {
            case "LT" -> factorValue < threshold;
            case "LTE" -> factorValue <= threshold;
            case "GT" -> factorValue > threshold;
            case "GTE" -> factorValue >= threshold;
            case "EQ" -> Double.compare(factorValue, threshold) == 0;
            default -> {
                log.warn("未知的比较运算符: {}", operator);
                yield false;
            }
        };
    }

    /**
     * 加载启用的配置，优先从 Redis 缓存读取
     */
    @SuppressWarnings("unchecked")
    private List<UserRiskFactorConfig> loadEnabledConfigs() {
        try {
            Object cached = redisTemplate.opsForValue().get(CONFIG_CACHE_KEY);
            if (cached instanceof List) {
                return (List<UserRiskFactorConfig>) cached;
            }
        } catch (Exception e) {
            log.warn("从 Redis 读取用户风险因子配置缓存失败，回退到数据库查询", e);
        }

        List<UserRiskFactorConfig> configs = userRiskFactorConfigMapper.findAllEnabled();

        try {
            redisTemplate.opsForValue().set(CONFIG_CACHE_KEY, configs,
                    CONFIG_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写入用户风险因子配置缓存到 Redis 失败", e);
        }

        return configs;
    }
}
