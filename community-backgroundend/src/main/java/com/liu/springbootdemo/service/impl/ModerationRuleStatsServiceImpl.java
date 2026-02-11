package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.vo.ModerationRuleStatsVO;
import com.liu.springbootdemo.mapper.ModerationRuleStatsMapper;
import com.liu.springbootdemo.service.ModerationRuleStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 规则命中统计服务实现
 */
@Service
@Slf4j
public class ModerationRuleStatsServiceImpl implements ModerationRuleStatsService {

    @Autowired
    private ModerationRuleStatsMapper moderationRuleStatsMapper;

    @Override
    public void incrementHitCounts(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return;
        }
        for (Long ruleId : ruleIds) {
            try {
                moderationRuleStatsMapper.incrementHitCount(ruleId);
            } catch (Exception e) {
                log.warn("更新规则命中统计失败, ruleId={}", ruleId, e);
            }
        }
    }

    @Override
    public ModerationRuleStatsVO getStatsByRuleId(Long ruleId) {
        return moderationRuleStatsMapper.findByRuleId(ruleId);
    }

    @Override
    public List<ModerationRuleStatsVO> getAllStats() {
        return moderationRuleStatsMapper.findAllStats();
    }
}
