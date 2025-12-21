package com.liu.springbootdemo.POJO.dto;

import lombok.Data;

@Data
public class LoginInControllerDTO { //TODO:可能要加@NotNULL等，还有更新登录模块时可以版本管理一下
    private String usernameOrEmail;
    private String password;

    public LoginInControllerDTO(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
}
