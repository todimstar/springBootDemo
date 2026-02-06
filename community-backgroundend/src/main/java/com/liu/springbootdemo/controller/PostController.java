package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.dto.CreatePostDTO;
import com.liu.springbootdemo.POJO.entity.Post;
import com.liu.springbootdemo.POJO.Result.Result;
import com.liu.springbootdemo.POJO.vo.PostDetailVO;
import com.liu.springbootdemo.POJO.vo.PostSummaryVO;
import com.liu.springbootdemo.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     *
     * @param createPostDTO
     * @return 成功返回200和帖子内容
     */
    @PostMapping()
    @Operation(summary = "创建帖子", description = "创建一个新的帖子，返回创建成功的帖子内容,分区Id必传")
    public ResponseEntity<Result<PostDetailVO>> createPost(@Validated @RequestBody CreatePostDTO createPostDTO){ //NOTE:12.31升级DTO但不用VO     //NOTE:12.28 升级了分区在post里必传
        // Controller只负责从网络获取用户并将id传参和调用Service
        PostDetailVO createPost = postService.createPost(createPostDTO);
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
    public Result<PostDetailVO> updatePost(@PathVariable("id") Long postId, @RequestBody Post post){
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
    public Result deletePost(@PathVariable("id") Long postId){
        log.debug("要删除的postId={}", postId);

        postService.deletePost(postId);
        return Result.success();
    }


    /**
     * 获取帖子，过滤器不设检查，会自动修正分页，默认1页,20行
     * 只返回公开帖子，用于首页展示
     * v1.2.0 升级VO和用Pageable分页
     * @return  200和当页帖子简要列表
     */
    @GetMapping()
    @SecurityRequirements() // 标记此接口不需要鉴权
    public Result<PageResult> pagePostSummary(@PageableDefault(page = 0,size = 20) Pageable pageable){
        return Result.success(postService.pagePostSummary(pageable));
     }   //默认返回200

    /**
     * 通过{userId}分页获取某用户的帖子
     * -正常用户和管理员都能获取
     * v1.2.0 修改VO为SummaryVO
     * v1.2.1 修复用户还能查询到被删除的帖子的问题，并完善根据beartoken判断是否为管理员的逻辑，同时允许游客按照非作者身份查看
     * 一共三种状态，游客和普通用户只能看已发布的，作者能看除已删除外的所有自己的帖子，管理员能看所有帖
     * @param userId
     * @param pageable
     * @return
     */ //OK:三种状态已经实现本接口正常功能完工
    @GetMapping("user/{userId}")
    public Result<PageResult> pagePostsByUserId(@Min(value = 1,message = "userId不能小于1")
                                                @NotNull
                                                @PathVariable Long userId,
                                                @PageableDefault(page = 0,size = 20) Pageable pageable){

        return Result.success(postService.pagePostsByUserId(userId, pageable));
    }

    /**
     * 获取单个帖子
     * 接口会根据bearToken动态判断用户身份返回值，游客和普通用户只能看已发布，作者能多看见自身未发布的帖子，管理员能看所有状态的帖子
     * v1.2.0:升级为DetailVO
     * v1.2.1:完善根据beartoken判断身份的逻辑，允许游客查看已发布帖子
     * @return
     */
    @GetMapping("/{id}")
    @SecurityRequirements() // 此接口不强制需要鉴权，但会根据有无token和token身份返回不同结果
    public Result<PostDetailVO> getPostById(@PathVariable("id") Long postId){ //TODO:之后管理员后台审核帖子也需要一个鉴权的get去获取被封禁的帖子，再说现在还没有这个状态，状态太多了，先不搞
        return Result.success(postService.getPostById(postId));
    }


    /**
     * 游标分页获取帖子列表（无限滚动高性能版）
     * 只返回已发布的帖子List，用于首页展示
     * 没有页码，只有上一页最后一条的ID作为游标cursor传入
     * 前端若收到列表.size < size则代表没有下一页了
     * @param cursor 上一页最后一条ID
     * @param size 每页条数
     * @return 列表
     */
    @GetMapping("/feed")
    @SecurityRequirements()
    public Result<List<PostSummaryVO>> getPostsByCursor(
            @RequestParam(required = false) Long cursor,    //null,<=0都可以，null不查，<=0数据库返回空
            @RequestParam(defaultValue = "10") @Min(1) int size
    ){
        return Result.success(postService.getPostsByCursor(cursor, size));
    }
}
