package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.POJO.vo.Result.Result;
import com.liu.springbootdemo.POJO.entity.Comment;
import com.liu.springbootdemo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    //-----------------------------Comment评论API处理-------------------------------
    @PutMapping("{postId}/comments")
    public ResponseEntity<Result<Comment>> createComment(@PathVariable("postId") Long postId, @RequestBody Comment comment){
        Comment returnComment = commentService.createComment(postId,comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(returnComment));
    }

    /**
     * 分页获取postId下评论
     * @param postId
     * @param page
     * @param size
     * @return  200和评论数组
     */
    @GetMapping("{postId}/comments")
    public Result<List<Comment>> getCommentByPostIdByPage(
            @PathVariable("postId") Long postId,
            @RequestParam(value = "page",defaultValue = "1",required = false) int page,
            @RequestParam(value = "size",defaultValue = "10",required = false) int size){
        if(page<1)page = 1;
        if(size<1)size = 10;
        if(size>100)size =100;
        return Result.success(commentService.findByPostIdByPage(postId,page,size));
    }

    @DeleteMapping("{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId){
        System.out.println("要删除的评论id="+commentId);

        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
