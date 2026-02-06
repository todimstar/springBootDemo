package com.liu.springbootdemo.POJO.vo;

import lombok.Data;

@Data
public class LoginResponseVO { //TODO:这是VO之后改名字和位置一下

    private String username;
    private String jwtToken;

    public LoginResponseVO(String username, String jwtToken) {
        this.username = username;
        this.jwtToken = jwtToken;
    }
}
