package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.vo.ModerationRuleVO;

/**
 * 审核规则管理服务接口
 */
public interface ModerationRuleService {

    /**
     * 分页查询规则列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param ruleType 规则类型筛选（keyword/regex/blacklist），null 为全部
     * @param enabled  启用状态筛选，null 为全部
     * @return 分页结果
     */
    PageResult pageQuery(Integer page, Integer pageSize, String ruleType, Boolean enabled);

    /**
     * 新增规则
     *
     * @param ruleType    规则类型
     * @param pattern     匹配模式
     * @param weightScore 权重分
     * @param enabled     是否启用
     * @param description 规则描述
     * @return 新增规则的 VO
     */
    ModerationRuleVO createRule(String ruleType, String pattern, int weightScore,
                                boolean enabled, String description);

    /**
     * 修改规则
     *
     * @param id          规则 ID
     * @param ruleType    规则类型
     * @param pattern     匹配模式
     * @param weightScore 权重分
     * @param enabled     是否启用
     * @param description 规则描述
     * @return 更新后的规则 VO
     */
    ModerationRuleVO updateRule(Long id, String ruleType, String pattern,
                                Integer weightScore, Boolean enabled, String description);

    /**
     * 软删除规则
     *
     * @param id 规则 ID
     */
    void deleteRule(Long id);
}
