package com.liu.springbootdemo.POJO.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginInControllerDTO { //TODO:可能要加@NotNULL等，还有更新登录模块时可以版本管理一下
    private String usernameOrEmail;
    private String password;
}
