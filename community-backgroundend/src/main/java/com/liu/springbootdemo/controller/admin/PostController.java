package com.liu.springbootdemo.controller.admin;

import com.liu.springbootdemo.POJO.Result.Result;
import com.liu.springbootdemo.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminPostController")
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("api/admin/posts")
@Validated
@Slf4j
@Tag(name = "管理员帖子管理接口", description = "管理员对帖子进行管理的相关接口")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("/{postId}")
    public Result setPostStatus(@PathVariable Long postId,
                                @Max(value = 4)
                              @Min(value = 0)
                              @NotNull
                              int status) {
        postService.setPostStatus(postId, status);
        return Result.success();
    }
}
