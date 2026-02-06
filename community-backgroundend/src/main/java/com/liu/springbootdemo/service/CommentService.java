package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.entity.Comment;

import java.util.List;

public interface CommentService {
    Comment createComment(Long postId,Comment comment);

    List<Comment> findByPostIdByPage(Long postId,int index,int size);

    void deleteComment(Long commentId);
}
