package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.dto.ModerationDecisionResult;
import com.liu.springbootdemo.POJO.entity.User;

/**
 * 审核决策服务接口
 * 根据风险评分阈值自动决策内容状态
 */
public interface ModerationDecisionService {

    /**
     * 对内容进行审核决策
     *
     * @param text       内容文本
     * @param user       发布者
     * @param targetId   目标ID（帖子ID或评论ID）
     * @param targetType 目标类型（post/comment）
     * @return 审核决策结果
     */
    ModerationDecisionResult decide(String text, User user, Long targetId, String targetType);
}
