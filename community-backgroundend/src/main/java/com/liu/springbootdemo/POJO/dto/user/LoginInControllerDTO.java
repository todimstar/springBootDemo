package com.liu.springbootdemo.POJO.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginInControllerDTO { //TODO:可能要加@NotNULL等，还有更新登录模块时可以版本管理一下
    @NotNull
    @NotBlank(message = "用户名或邮箱不能为空")
    private String usernameOrEmail;
    @NotNull
    @NotBlank(message = "密码不能为空")
    private String password;
}
