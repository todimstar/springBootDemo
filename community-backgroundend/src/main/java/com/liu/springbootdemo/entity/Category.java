package com.liu.springbootdemo.entity;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分区表，一对多
 */
@Data
public class Category {
    private Long id;    //分区id                      不变
    private String name;    //分区名                   管理员
    private String description;//分区描述               管理员
    private String icon;    //分区图标地址              管理员
    private int postCount;  //帖子数量                  自动变
    private int sortOrder;  //分区排序权重 ，越大越靠前   管理员
    private Boolean isActive;//分区是否活动,默认1活动    管理员
    private LocalDateTime createTime;//分区创建时间    不会变

}
