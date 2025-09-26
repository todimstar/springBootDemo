package com.liu.springbootdemo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private Long id;        //帖子id 主键
    private Long userId;    //发表用户id 多创个索引优化查询
    private String title;   //外显标题
    private String content; // 内容
    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//修改时间
}
