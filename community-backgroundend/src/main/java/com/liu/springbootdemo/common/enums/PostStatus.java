package com.liu.springbootdemo.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostStatus {
    DRAFT(0),       //草稿，仅用户可见
    PENDING_REVIEW(1),//待审核
    PUBLISHED(2),   //已发布，审核通过
    REJECTED(3),    //审核未通过,拒绝
    DELETED(4);     //已删除，软删除，6个月后物理删除

    private final int status;

}
