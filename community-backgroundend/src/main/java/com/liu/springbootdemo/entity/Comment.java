package com.liu.springbootdemo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;    //评论主键                      ，创建时数据库生成
    private Long postId;    //外键帖子id                ，Service中检查
    private Long userId;    //评论者id                  ，Service中检查与当前登录用户是否相等
    private String content; //评论内容                  ，Service中检查是否为空
    private LocalDateTime createTime;   //评论创建时间   ，数据库负责
    private String ipAddress;  //评论者ip               ，待实现
}
