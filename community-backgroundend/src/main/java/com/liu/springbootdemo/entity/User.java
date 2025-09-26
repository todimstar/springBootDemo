package com.liu.springbootdemo.entity;


import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;


/**
 * Users表的实体类
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createTime;   //用户创建时间
    private LocalDateTime lastLoginTime;   //用户最后登录时间
    private String role;


}
