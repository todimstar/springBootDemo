package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.dto.LoginResponseDTO;
import com.liu.springbootdemo.entity.User;

import java.util.List;

public interface UserService {
    // 定义注册用户的业务方法
    void register(User user);

    // 定义登录用户的业务方法
    LoginResponseDTO login(String username, String password);

    // 获取所有用户
    List<User> getAllUser();

}
