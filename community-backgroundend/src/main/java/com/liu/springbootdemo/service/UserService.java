package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.dto.user.RegisterDTO;
import com.liu.springbootdemo.POJO.dto.user.UpdateUserDTO;
import com.liu.springbootdemo.POJO.vo.LoginResponseVO;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.vo.UpdateUserVO;
import jakarta.validation.constraints.Email;

import java.util.List;

public interface UserService {
    // 定义注册用户的业务方法
    void register(RegisterDTO registerDTO);

    // 定义登录用户的业务方法
    LoginResponseVO login(String username, String password);

    void sendVerificationCode(String email, String message);

    // 获取所有用户
    List<User> getAllUser();

    User getUserById(Long id);

    /**
     * 更新用户信息
     * @param updateUserDTO
     * @return UpdateUserVO
     */
    UpdateUserVO updateUser(UpdateUserDTO updateUserDTO);

    /**
     * 管理员硬删除用户
     * @param id
     */
    void deleteHeadByIdForAdmin(Long id);

    void sendRegisterCode(@Email String email);
}
