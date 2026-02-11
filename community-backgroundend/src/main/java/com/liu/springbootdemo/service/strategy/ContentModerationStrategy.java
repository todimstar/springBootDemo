package com.liu.springbootdemo.service.strategy;

import com.liu.springbootdemo.POJO.dto.RiskScoreResult;
import com.liu.springbootdemo.POJO.entity.User;

/**
 * 内容审核策略接口
 * <p>
 * 不同内容类型（文本、图片等）实现该接口，
 * 审核引擎通过策略模式按内容类型分发到对应的审核实现。
 */
public interface ContentModerationStrategy {

    /**
     * 判断当前策略是否支持指定的内容类型
     *
     * @param contentType 内容类型标识（如 "text"、"image"）
     * @return 是否支持
     */
    boolean supports(String contentType);

    /**
     * 对内容进行风险评分
     *
     * @param content 待审核的内容（文本为原文，图片为 URL 或 Base64）
     * @param user    发布内容的用户
     * @return 风险评分结果
     */
    RiskScoreResult evaluate(String content, User user);
}
