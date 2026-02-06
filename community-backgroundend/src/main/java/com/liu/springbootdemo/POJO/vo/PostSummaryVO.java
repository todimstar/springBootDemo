package com.liu.springbootdemo.POJO.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "帖子列表展示简要VO", description = "没有content,且除了post表还会联动获取categoryName、userName、userAvatarUrl、likeCount、collectCount等表的信息")//TODO:文件上传实现后注意这里的两个url是否需要调整
public class PostSummaryVO {

    private Long id;        //帖子id 主键
    private Long userId;    //发表用户id 给前端做用户主页链接用

    private String title;   //外显标题
    private String summary; // 帖子摘要/简介
    private String coverImage; //封面图URL

    private String username;    //用户名
    private String userAvatarUrl;  //用户头像url    TODO:也许之后还可以加用户等级之类的信息

    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//修改时间
    private String categoryName;//分区名称，Mapping用left去category表查

    private int viewCount; //浏览量
    private int likeCount; //点赞数
    private int collectCount;  //收藏数
    private int commentCount;  //评论量

    private boolean isPinned;  //是否置顶
    private boolean isEssence; //是否精华
    private int status; //状态：0草稿draft，1待审核pending_review，2已发布published,3已拒绝，4已删除；可能要给作者按状态来排序
}
