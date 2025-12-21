package com.liu.springbootdemo.POJO.dto;

import lombok.Data;

@Data
public class LoginResponseDTO { //TODO:这是VO之后改名字和位置一下

    private String username;
    private String jwtToken;

    public LoginResponseDTO(String username, String jwtToken) {
        this.username = username;
        this.jwtToken = jwtToken;
    }
}
