package com.liu.springbootdemo.entity.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class LoginResponseDTO {

    private String username;
    private String jwtToken;

    public LoginResponseDTO(String username, String jwtToken) {
        this.username = username;
        this.jwtToken = jwtToken;
    }
}
