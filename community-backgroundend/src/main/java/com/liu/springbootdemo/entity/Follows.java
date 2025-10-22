package com.liu.springbootdemo.entity;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关注表，多对多关系连接表
 */
@Data
public class Follows {
    private Long id;    //关注关系id
    private Long followerId;    //关注用户ID
    private Long followeeId;    //被关注用户ID
    private LocalDateTime createTime;//关注时间戳
}
