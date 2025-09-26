package com.liu.springbootdemo.entity.DTO;

import lombok.Data;

@Data
public class LoginInControllerDTO {
    private String usernameOrEmail;
    private String password;

    public LoginInControllerDTO(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
}
