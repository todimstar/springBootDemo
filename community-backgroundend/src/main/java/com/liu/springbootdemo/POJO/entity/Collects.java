package com.liu.springbootdemo.POJO.entity;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏表，多对多关系连接表
 */
@Data
public class Collects {
    private Long id;    //收藏关系id
    private Long userId;    //收藏用户ID
    private Long postId;    //收藏帖子ID
    private LocalDateTime createTime;//收藏时间戳
}
