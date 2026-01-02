package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.dto.CreatePostDTO;
import com.liu.springbootdemo.POJO.entity.Post;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface PostService {
    Post createPost(CreatePostDTO createPostDTO);

    Post updatePost(Long postId, Post post);

    void deletePost(Long postId);

    Post getPostById(Long postId);

    List<Post> getPostsByPage(int page,int size);

    List<Post> getAllTitles();

    PageResult pagePostsByUserId(Long userId, Pageable pageable);
}
