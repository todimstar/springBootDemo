package com.liu.springbootdemo.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.entity.ModerationRule;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.vo.ModerationRuleVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.enums.RuleType;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.common.utils.SecurityUtil;
import com.liu.springbootdemo.mapper.ModerationRuleMapper;
import com.liu.springbootdemo.service.ModerationAuditLogService;
import com.liu.springbootdemo.service.ModerationRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审核规则管理服务实现
 */
@Service
@Slf4j
public class ModerationRuleServiceImpl implements ModerationRuleService {

    private static final String RULES_CACHE_KEY = "moderation:rules:enabled";

    @Autowired
    private ModerationRuleMapper moderationRuleMapper;

    @Autowired
    private ModerationAuditLogService auditLogService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult pageQuery(Integer page, Integer pageSize, String ruleType, Boolean enabled) {
        PageHelper.startPage(page, pageSize);
        Page<ModerationRuleVO> voPage = moderationRuleMapper.pageQuery(ruleType, enabled);
        return new PageResult(voPage.getTotal(), voPage.getResult());
    }

    @Override
    @Transactional
    public ModerationRuleVO createRule(String ruleType, String pattern, int weightScore,
                                       boolean enabled, String description) {
        // 校验规则类型
        validateRuleType(ruleType);

        ModerationRule rule = new ModerationRule();
        rule.setRuleType(ruleType);
        rule.setPattern(pattern);
        rule.setWeightScore(weightScore);
        rule.setEnabled(enabled);
        rule.setDescription(description != null ? description : "");

        moderationRuleMapper.insert(rule);
        log.info("新增审核规则: id={}, type={}, pattern={}", rule.getId(), ruleType, pattern);

        // 刷新缓存
        refreshRulesCache();

        // 写入审计日志
        logRuleChange(rule.getId(), "rule_create",
                String.format("新增规则: type=%s, pattern=%s, weight=%d", ruleType, pattern, weightScore));

        return toVO(moderationRuleMapper.findById(rule.getId()));
    }

    @Override
    @Transactional
    public ModerationRuleVO updateRule(Long id, String ruleType, String pattern,
                                       Integer weightScore, Boolean enabled, String description) {
        ModerationRule existing = moderationRuleMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "规则不存在");
        }

        // 校验规则类型
        if (ruleType != null) {
            validateRuleType(ruleType);
        }

        ModerationRule rule = new ModerationRule();
        rule.setId(id);
        rule.setRuleType(ruleType);
        rule.setPattern(pattern);
        if (weightScore != null) {
            rule.setWeightScore(weightScore);
        }
        rule.setEnabled(enabled != null ? enabled : existing.isEnabled());
        rule.setDescription(description);

        moderationRuleMapper.update(rule);
        log.info("更新审核规则: id={}", id);

        // 刷新缓存
        refreshRulesCache();

        // 写入审计日志
        logRuleChange(id, "rule_update", buildUpdateReason(ruleType, pattern, weightScore, enabled, description));

        return toVO(moderationRuleMapper.findById(id));
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        ModerationRule existing = moderationRuleMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "规则不存在");
        }

        int affected = moderationRuleMapper.softDelete(id);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "规则不存在或已删除");
        }
        log.info("软删除审核规则: id={}", id);

        // 刷新缓存
        refreshRulesCache();

        // 写入审计日志
        logRuleChange(id, "rule_delete",
                String.format("删除规则: type=%s, pattern=%s", existing.getRuleType(), existing.getPattern()));
    }

    /**
     * 校验规则类型
     */
    private void validateRuleType(String ruleType) {
        try {
            RuleType.fromValue(ruleType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "非法规则类型: " + ruleType);
        }
    }

    /**
     * 刷新规则缓存
     */
    private void refreshRulesCache() {
        try {
            redisTemplate.delete(RULES_CACHE_KEY);
            log.info("规则缓存已刷新");
        } catch (Exception e) {
            log.warn("刷新规则缓存失败", e);
        }
    }

    /**
     * 记录规则变更审计日志
     */
    private void logRuleChange(Long ruleId, String actionType, String reason) {
        User currentUser = SecurityUtil.getCurrentUser();
        Long operatorId = currentUser != null ? currentUser.getId() : null;
        String operatorName = currentUser != null ? currentUser.getUsername() : "UNKNOWN";
        auditLogService.logManualAction(ruleId, "rule", actionType,
                operatorId, operatorName, reason, null, 0);
    }

    /**
     * 构建更新操作的原因描述
     */
    private String buildUpdateReason(String ruleType, String pattern, Integer weightScore,
                                     Boolean enabled, String description) {
        StringBuilder sb = new StringBuilder("更新规则:");
        if (ruleType != null) sb.append(" type=").append(ruleType);
        if (pattern != null) sb.append(" pattern=").append(pattern);
        if (weightScore != null) sb.append(" weight=").append(weightScore);
        if (enabled != null) sb.append(" enabled=").append(enabled);
        if (description != null) sb.append(" desc=").append(description);
        return sb.toString();
    }

    /**
     * 实体转 VO
     */
    private ModerationRuleVO toVO(ModerationRule rule) {
        if (rule == null) return null;
        return new ModerationRuleVO(
                rule.getId(), rule.getRuleType(), rule.getPattern(),
                rule.getWeightScore(), rule.isEnabled(), rule.getDescription(),
                rule.getCreatedAt(), rule.getUpdatedAt()
        );
    }
}
