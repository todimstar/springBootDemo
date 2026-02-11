package com.liu.springbootdemo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核操作类型枚举
 */
@Getter
@AllArgsConstructor
public enum ModerationAction {
    APPROVE("approve", "通过", PostStatus.PUBLISHED.getStatus()),
    REJECT("reject", "驳回", PostStatus.REJECTED.getStatus()),
    TAKEDOWN("takedown", "下架", PostStatus.DELETED.getStatus()),
    SHADOW_BAN("shadow_ban", "影子发布", PostStatus.SHADOW_BANNED.getStatus());

    private final String code;
    private final String description;
    private final int targetPostStatus;

    public static ModerationAction fromCode(String code) {
        for (ModerationAction action : values()) {
            if (action.code.equals(code)) {
                return action;
            }
        }
        return null;
    }
}
