package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.vo.ModerationRuleStatsVO;

import java.util.List;

/**
 * 规则命中统计服务接口
 */
public interface ModerationRuleStatsService {

    /**
     * 批量递增规则命中次数（原子操作）
     *
     * @param ruleIds 命中的规则 ID 列表
     */
    void incrementHitCounts(List<Long> ruleIds);

    /**
     * 查询单条规则统计
     *
     * @param ruleId 规则 ID
     * @return 规则统计 VO
     */
    ModerationRuleStatsVO getStatsByRuleId(Long ruleId);

    /**
     * 查询所有规则统计摘要
     *
     * @return 全量规则统计列表，按命中次数降序
     */
    List<ModerationRuleStatsVO> getAllStats();
}
