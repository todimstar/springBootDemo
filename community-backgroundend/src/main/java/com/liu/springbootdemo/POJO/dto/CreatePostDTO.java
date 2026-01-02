package com.liu.springbootdemo.POJO.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "创建帖子请求 DTO")
public class CreatePostDTO {
    
    @NotNull
    @NotBlank
    @Size(max = 50, message = "标题不能超过{max}个字符")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "帖子标题")
    private String title;   //外显标题
    @NotNull
    @NotBlank
    @Size(max = 50000, message = "内容不能超过{max}个字符")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "帖子内容")
    private String content; // 内容
    @NotNull
    @NotBlank
    @Size(max = 300, message = "简介不能超过{max}个字符")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "帖子摘要/简介")
    private String summary; // 帖子摘要/简介

    @NotNull(message = "分类不能为空")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "分区ID")  //算了直接前端传id过来好了，不然还要名字去查id麻烦，每次显示帖子都查一遍或者多存一个字段
    private Long categoryId;    //分区id
    @Size(max = 255, message = "链接不能超过{max}字符长度")
    private String coverImage; //封面图URL

//    private boolean isPinned;  //是否置顶
//    private boolean isEssence; //是否精华
    private String ipAddress; //发布者IP
}
