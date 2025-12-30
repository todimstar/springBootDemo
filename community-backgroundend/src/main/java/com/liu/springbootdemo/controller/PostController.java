package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.entity.Post;
import com.liu.springbootdemo.POJO.Result.Result;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@Validated
@Slf4j
@Tag(name = "PostController", description = "帖子相关接口")
public class PostController {
    @Autowired
    private PostService postService;

    /**
     * 创建帖子
     * @param post
     * @return 成功返回200和帖子内容
     */
    @PostMapping()
    @Operation(summary = "创建帖子", description = "创建一个新的帖子，返回创建成功的帖子内容,分区Id必传")
    public ResponseEntity<Result<Post>> createPost(@Validated @RequestBody Post post){ //NOTE:12.28 升级了分区在post里必传
        // Controller只负责从网络获取用户并将id传参和调用Service
        Post createPost = postService.createPost(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(createPost));
    }

    /**
     * 更新帖子，会检查当前登录用户权限是否属于帖子,只能更新自己的帖子,管理端有新接口但是Service层共用
     * @param postId
     * @param post
     * @return  成功返回200和更新后帖子
     */
    @PatchMapping("/{id}")  //Patch可能会受到内网限制，不过本项目先实验使用此RESTFUL标准
    @Operation(summary = "更新帖子", description = "更新一个已有的帖子，返回更新成功的帖子内容\n会检查当前登录用户权限是否属于帖子,只能更新自己的帖子")
    public Result updatePost(@PathVariable("id") Long postId, @RequestBody Post post){
        //结果是一样的，还是Patch更完善
        post.setId(postId); //必须改不然后续Service或者其他代码使用了post里的id就会改错帖子
        return Result.success(postService.updatePost(postId,post));    //默认返回则是200
    }

    /**
     * 删除帖子，会检查当前登录用户权限是否属于帖子,只能删除自己的帖子，管理员会有新接口但是Service层共用
     * @param postId
     * @return  成功返回204无内容
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除帖子", description = "删除一个已有的帖子，会检查当前登录用户权限是否属于帖子,只能删除自己的帖子")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long postId){
        log.debug("要删除的postId={}", postId);

        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
        //return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);// 204状态码，
    }


    /**
     * 获取帖子，过滤器不设检查，会自动修正分页，默认1页,10行，最大100行
     * v1.2.0 升级VO
     * @param page
     * @param size
     * @return  200和当页帖子列表
     */
    @GetMapping()
    @SecurityRequirements() // 标记此接口不需要鉴权
    public Result getPostsByPage(//TODO:改造为DTO和VO，传出需要<>
            @RequestParam(value = "page", defaultValue = "1", required = false) int page,   //保留早期型，之后可以用于测试接口速度
            @RequestParam(value = "size", defaultValue = "20", required = false) int size
    ){

       if(page<1)page = 1;     //修正请求为默认页
       if(size<1)size = 10;    //默认值10
       if(size>100)size = 100;

        return Result.success(postService.getPostsByPage(page,size));
     }   //默认返回200

    /**
     * 通过{userId}分页获取某用户的帖子,尝试Pageable
     * -正常用户和管理员都能获取
     * @param userId
     * @param pageable
     * @return
     */
    @GetMapping("user/{userId}")    //TODO: 12.29 ing 发现和上面getPostsByPage冲突了，需要改路径
    public Result<PageResult> pagePostsByUserId(@Min(value = 1,message = "userId不能小于1")
                                                @NotNull
                                                @PathVariable Long userId,
                                                @PageableDefault(page = 0,size = 20) Pageable pageable){

        return Result.success(postService.pagePostsByUserId(userId, pageable));
    }

    /**
     * 获取单个帖子
     *
     * @return
     */
    @GetMapping("/{id}")
    @SecurityRequirements() // 标记此接口不需要鉴权
    public Result getPostById(@PathVariable("id") Long postId){ //TODO:之后管理员后台审核帖子也需要一个鉴权的get去获取被封禁的帖子
        return Result.success(postService.getPostById(postId));
    }

    /**
     * 获取所有帖子标题列表
     * TODO:检查一下该帖子标题查询的是否为被禁用的也能被查询，还有是否需要分页，不记得应用场景是什么了，展示在首页的话还需要数据库自动多个，描述展示，存文章前100字之类的
     * @return
     */
    @GetMapping("/allTitles")
    @SecurityRequirements() // 标记此接口不需要鉴权
    public Result<List<Post>> getAllPostsTitle(){
        return Result.success(postService.getAllTitles());
    }



}
