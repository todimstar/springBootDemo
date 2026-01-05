package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.dto.CreatePostDTO;
import com.liu.springbootdemo.POJO.entity.Post;
import com.liu.springbootdemo.POJO.vo.PostDetailVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface PostService {
    PostDetailVO createPost(CreatePostDTO createPostDTO);

    PostDetailVO updatePost(Long postId, Post post);

    /**
     * 根据id获取帖子详情
     * @param postId
     * @return PostDetailVO
     */
    PostDetailVO getPostById(Long postId);

    /**
     * 分页获取帖子概要列表
     * @param pageable
     * @return PageResult<Total,Page<PostSummaryVO>>
     */
    PageResult pagePostSummary(Pageable pageable);

    /**
     * 分页获取某用户的帖子概要列表
     * @param userId
     * @param pageable
     * @return PageResult<Total,Page<PostSummaryVO>>
     */
    PageResult pagePostsByUserId(Long userId, Pageable pageable);

    /**
     * 删除ID对应帖子
     * @param postId
     */
    void deletePost(Long postId);

    /**
     * 管理员设置帖子状态
     * @param postId 帖子ID
     * @param status 状态码 0草稿，1待审核，2已发布，3已拒绝，4已删除
     */
    void setPostStatus(Long postId, @Max(value = 4) @Min(value = 0) @NotNull int status);
}
