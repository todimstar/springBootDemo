package com.liu.springbootdemo.POJO.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "单帖子具体展示视图对象 VO", description = "比summaryVO多content、发布者IP等具体信息，没有了summary信息") //TODO:前提是该点有新技术点不然感觉算业务：之后在该页面前端记得还要请求User的帖子数、关注、等级之类的信息；不知道关注这种算不算亮点不算就不搞了这样还需要user多几个关注接口和表要加上同样的冗余字段
public class PostDetailVO {

    private Long id;        //帖子id 让前端好找去跳转
    private String title;   //外显标题
    private String content; // 内容

    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//修改时间

    private String categoryName;//分区名称，Mapping用left去category表查
    private String coverImage; //封面图URL
    private int viewCount; //浏览量
    private int likeCount; //点赞数
    private int collectCount;  //收藏数
    private int commentCount;  //评论量

    private int status; //状态：0草稿draft，1待审核pending_review，2已发布published,3已拒绝，4已删除；可能要给作者看状态
    private boolean isPinned;  //是否置顶
    private boolean isEssence; //是否精华
    private String ipAddress; //发布者IP
}
