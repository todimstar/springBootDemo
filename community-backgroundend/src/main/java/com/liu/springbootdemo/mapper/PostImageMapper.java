package com.liu.springbootdemo.mapper;

import com.liu.springbootdemo.POJO.entity.PostImage;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostImageMapper {
    /**
     * 插入临时图片资源，默认标记为0 Temp
     * @param postImage
     * @return
     */
    int insertTempImage(PostImage postImage);



}
