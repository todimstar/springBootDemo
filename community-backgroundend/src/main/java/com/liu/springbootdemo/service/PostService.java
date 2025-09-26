package com.liu.springbootdemo.service;

import com.liu.springbootdemo.entity.Post;

import java.util.List;

public interface PostService {
    Post createPost(Post post);

    Post updatePost(Long postId, Post post);

    void deletePost(Long postId);

    Post getPostById(Long postId);

    List<Post> getPostsByPage(int page,int size);

    List<Post> getAllTitles();

}
