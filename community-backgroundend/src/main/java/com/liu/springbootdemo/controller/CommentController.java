package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.service.CommentService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @DeleteMapping("{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId){
        System.out.println("要删除的评论id="+commentId);

        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
