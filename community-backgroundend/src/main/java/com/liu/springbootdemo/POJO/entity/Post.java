package com.liu.springbootdemo.POJO.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "帖子实体类")
public class Post {
    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "帖子ID")
    private Long id;        //帖子id 主键
    private Long userId;    //发表用户id 多创个索引优化查询
//    @NotNull
//    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "帖子标题")
    private String title;   //外显标题
//    @NotNull
//    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "帖子内容")
    private String content; // 内容
    @NotNull
    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "帖子摘要/简介")
    private String summary; // 帖子摘要/简介
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//修改时间
    //新增
    @NotNull(message = "分类不能为空")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "分区ID")
    private Long categoryId;    //分区id
    private String categoryName;//分区名称,创建和更改时自动去category表查
    private String coverImage; //封面图URL
    private int viewCount; //浏览量
    private int likeCount; //点赞数
    private int collectCount;  //收藏数
    private int commentCount;  //评论量

    private boolean isPinned;  //是否置顶
    private boolean isEssence; //是否精华
    private String ipAddress; //发布者IP

    //内部状态字段
    private int status; //状态：0草稿draft，1待审核pending_review，2已发布published,3已拒绝，4已删除

    // 关联对象 (非数据库字段)
    private User author; // 作者信息，用于MyBatis关联查询
}
