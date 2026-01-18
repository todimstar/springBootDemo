package com.liu.springbootdemo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {
    AVATAR("avatars","用户头像",2*1024*1024),   //2MB限制
    POST_COVER("post-covers","帖子封面",5*1024*1024),
    POST_IMAGE("post-images","帖子中图片",5*1024*1024),
    COMMENT_IMAGE("comment-images","评论图片",3*1024*1024),
    OTHER("others","其他文件",10*1024*1024);

    //文件夹前缀，如"avatars"会有 bucket/{avatars}/2026/1/7/xxx.jpg
    private final String folder;
    //类型描述
    private final String description;
    //文件最大限制(字节)
    private final long maxSize;

    /**
     * 根据文件夹前缀查找枚举
     * 用于从 URL 反推文件类型
     */
    public static FileType fromFolder(String folder){
        for(FileType type:values()){
            if(type.folder.equals(folder)){
                return type;
            }
        }return OTHER;
    }

}
