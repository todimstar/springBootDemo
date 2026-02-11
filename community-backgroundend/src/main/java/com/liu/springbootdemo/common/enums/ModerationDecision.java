package com.liu.springbootdemo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核决策枚举
 * 根据风险评分阈值自动判定内容处置方式
 */
@Getter
@AllArgsConstructor
public enum ModerationDecision {
    AUTO_APPROVE("auto_approve", "自动放行", PostStatus.PUBLISHED.getStatus()),
    PENDING_REVIEW("pending_review", "送人审", PostStatus.PENDING_REVIEW.getStatus()),
    AUTO_REJECT("auto_reject", "自动拦截", PostStatus.REJECTED.getStatus());

    private final String code;
    private final String description;
    private final int postStatus;
}
