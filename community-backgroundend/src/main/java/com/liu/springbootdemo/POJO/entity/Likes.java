package com.liu.springbootdemo.POJO.entity;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞表，多对多，本表即为为了多对多而出的中间表
 */
@Data
public class Likes {
    private Long id;    //点赞id
    private Long userId;    //点赞用户id
    private Long targetId;  //目标id
    private int targetType; //点赞类型，1帖子，2评论
    private LocalDateTime createTime;   //点赞时间

}
