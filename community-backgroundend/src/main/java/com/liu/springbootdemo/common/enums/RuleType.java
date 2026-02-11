package com.liu.springbootdemo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核规则类型枚举
 */
@Getter
@AllArgsConstructor
public enum RuleType {
    KEYWORD("keyword"),
    REGEX("regex"),
    BLACKLIST("blacklist");

    private final String value;

    public static RuleType fromValue(String value) {
        for (RuleType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的规则类型: " + value);
    }
}
