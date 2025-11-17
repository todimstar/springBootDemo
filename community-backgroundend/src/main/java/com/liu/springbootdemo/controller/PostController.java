package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.entity.Comment;
import com.liu.springbootdemo.entity.Post;
import com.liu.springbootdemo.POJO.vo.Result;
import com.liu.springbootdemo.entity.User;
import com.liu.springbootdemo.service.CommentService;
import com.liu.springbootdemo.service.PostService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Controller层
 * 接收请求
 * 参数校验与解析
 * 调用Service层
 * 返回统一的响应结果
 */
@RestController
@RequestMapping("api/posts")
//@Validated
public class PostController {
    @Autowired
    private PostService postService;

    /**
     * 创建帖子
     * @param post
     * @return 成功返回200和帖子内容
     */
    @PostMapping()
    public ResponseEntity<Result<Post>> createPost(@AuthenticationPrincipal User user, @RequestBody Post post){ //稍显分层的安全获取user，不过本项目之前已经设计用了SecurityUntil.getCurrentUser()获取，这里只用单次实验@AuthenticationPrincipal会用ok
        // Controller只负责从网络获取用户并将id传参和调用Service
        Post createPost = postService.createPost(user.getId(),post);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(createPost));
    }

    /**
     * 更新帖子，会检查当前登录用户
     * @param postId
     * @param post
     * @return  成功返回200和更新后帖子
     */
    @PatchMapping("/{id}")
    public Result updatePost(@PathVariable("id") Long postId, @RequestBody Post post){
        //结果是一样的，还是Patch更完善
        post.setId(postId); //必须改不然后续Service或者其他代码使用了post里的id就会改错帖子
        return Result.success(postService.updatePost(postId,post));    //默认返回则是200
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long postId){
        System.out.println("要删除的postId="+postId);

        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
        //return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);// 204状态码，
    }


    /**
     * 获取帖子，过滤器不设检查，会自动修正分页，默认1页,10行，最大100行
     * @param page
     * @param size
     * @return  200和当页帖子列表
     */
    @GetMapping()
    public Result getPostsByPage(//TODO:改造为DTO和VO，传出需要<>
            @RequestParam(value = "page", defaultValue = "1", required = false)
//            @Min(1)
            int page,
            @RequestParam(value = "size", defaultValue = "20", required = false)
//                                     @Min(10) @Max(100)
                                     int size
    ){

       if(page<1)page = 1;     //修正请求为默认页
       if(size<1)size = 10;    //默认值10
       if(size>100)size = 100;

        return Result.success(postService.getPostsByPage(page,size));
     }   //默认返回200

    /**
     * 获取单个帖子
     *
     * @return
     */
    @GetMapping("/{id}")
    public Result getPostById(@PathVariable("id") Long postId){
        return Result.success(postService.getPostById(postId));
    }

    @GetMapping("/allTitles")
    public Result<List<Post>> getAllPostsTitle(){
        return Result.success(postService.getAllTitles());
    }



}
