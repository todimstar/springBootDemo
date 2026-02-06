package com.liu.springbootdemo.POJO.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
/**
 * 用户注册DTO
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;    //用户名

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少为6位")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;    //密码

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;   //邮箱

    private String avatarUrl;  //头像url
    private int gender; //0保密，1男，2女
    private String bio; //个人简介
    private String location;    //所在地，城市

    @NotBlank(message = "验证码不能为空")
    private String verCode; //邮箱验证码

}
