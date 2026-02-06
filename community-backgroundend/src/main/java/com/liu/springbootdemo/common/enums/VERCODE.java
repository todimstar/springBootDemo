package com.liu.springbootdemo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum VERCODE {
    REGISTER("注册", "VERCODE:REGISTER:", 10), //注册验证码，10分钟过期
    RESET_PASSWORD("重置密码", "VERCODE:RESET_PASSWORD:", 5); //重置密码验证码，5分钟过期
    private final String codeType;
    private final String redisKey;
    private final long timeoutMinutes;
}
