package com.liu.springbootdemo.POJO.vo;

import lombok.Data;

@Data
public class LoginResponseVO { //TODO:这是VO之后改名字和位置一下

    private String username;
    private String jwtToken;
    private String role;

    public LoginResponseVO(String username, String jwtToken, String role) {
        this.username = username;
        this.jwtToken = jwtToken;
        this.role = role;
    }
}
