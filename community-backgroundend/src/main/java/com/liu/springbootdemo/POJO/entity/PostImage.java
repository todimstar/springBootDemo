package com.liu.springbootdemo.POJO.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子图片关系表映射实体
 */
@Data
@Schema(description = "帖子图片关系表映射实体")
public class PostImage {

    private Long id;
    private Long postId;
    private String imageUrl;
    private String objectName;
    @Schema(description = "0 Temp, 1 Used, 默认0")
    private int status;    //0 Temp,1 Used
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
