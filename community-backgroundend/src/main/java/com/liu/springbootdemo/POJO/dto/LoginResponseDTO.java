package com.liu.springbootdemo.POJO.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {

    private String username;
    private String jwtToken;

    public LoginResponseDTO(String username, String jwtToken) {
        this.username = username;
        this.jwtToken = jwtToken;
    }
}
