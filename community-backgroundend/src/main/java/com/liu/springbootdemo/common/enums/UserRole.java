package com.liu.springbootdemo.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
    USER("ROLE_USER"),       //普通用户
    MODERATOR("ROLE_MODERATOR"),  //版主
    ADMIN("ROLE_ADMIN");       //管理员

    private final String roleName;

    public String getRoleName() {
        return this.roleName;
    }
}
