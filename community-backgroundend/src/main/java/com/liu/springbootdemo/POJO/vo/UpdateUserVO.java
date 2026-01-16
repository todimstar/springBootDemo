package com.liu.springbootdemo.POJO.vo;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户更新后返回的视图对象
 */
@Data
public class UpdateUserVO {
    private Long id;        //用户id
    private String username;    //用户名
    private String email;   //邮箱
    private LocalDateTime createTime;   //用户创建时间
    private LocalDateTime lastLoginTime;   //用户最后登录时间
    private String role;    //用户角色
    private String avatarUrl;  //头像url
    private int gender; //0保密，1男，2女
    private String bio; //个人简介
    private String location;    //所在地，城市
    private int points; //积分
    private int level;  //用户等级
    private boolean isBanned;  //是否被封禁
    private String banReason;   //封禁原因
    private LocalDateTime banUntil;    //封禁截止时间
}
