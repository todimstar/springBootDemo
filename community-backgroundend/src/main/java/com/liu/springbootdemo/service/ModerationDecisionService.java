package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.dto.ModerationDecisionResult;
import com.liu.springbootdemo.POJO.entity.User;

/**
 * 审核决策服务接口
 * 根据风险评分阈值自动决策内容状态
 */
public interface ModerationDecisionService {

    /**
     * 对内容进行审核决策（默认按文本类型审核）
     *
     * @param text       内容文本
     * @param user       发布者
     * @param targetId   目标ID（帖子ID或评论ID）
     * @param targetType 目标类型（post/comment）
     * @return 审核决策结果
     */
    ModerationDecisionResult decide(String text, User user, Long targetId, String targetType);

    /**
     * 对内容进行审核决策，按内容类型分发到对应的审核策略
     *
     * @param content     内容（文本原文或图片 URL 等）
     * @param contentType 内容类型（"text"、"image" 等）
     * @param user        发布者
     * @param targetId    目标ID
     * @param targetType  目标类型（post/comment）
     * @return 审核决策结果
     */
    ModerationDecisionResult decide(String content, String contentType, User user, Long targetId, String targetType);
}
