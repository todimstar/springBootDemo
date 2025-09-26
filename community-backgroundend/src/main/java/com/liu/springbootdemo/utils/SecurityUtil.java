package com.liu.springbootdemo.utils;

import com.liu.springbootdemo.entity.User;
import com.liu.springbootdemo.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    private static UserMapper staticUserMapper;

    @Autowired
    private UserMapper userMapper;

    @PostConstruct
    public void init(){
        staticUserMapper = userMapper;
    }

    /**
     * 从Security中获取正在登录的用户
     * @return  如果有就返回currentUser，否则为空
     */
    public static User getCurrentUser(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(principal instanceof UserDetails){
            UserDetails userDetails = (UserDetails) principal;
            return staticUserMapper.findByUsername(userDetails.getUsername());
        }
        return null;
    }
}
