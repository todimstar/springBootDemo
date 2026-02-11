package com.liu.springbootdemo.controller.admin;

import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.Result.Result;
import com.liu.springbootdemo.POJO.dto.request.ModerationActionDTO;
import com.liu.springbootdemo.POJO.dto.request.ModerationQueuePageQueryDTO;
import com.liu.springbootdemo.POJO.vo.ModerationQueueVO;
import com.liu.springbootdemo.service.ModerationQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController("adminModerationController")
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("api/admin/moderation")
@Validated
@Slf4j
@Tag(name = "管理员审核管理接口", description = "审核队列查询与操作")
public class ModerationController {

    @Autowired
    private ModerationQueueService moderationQueueService;

    /**
     * 分页查询待审核列表
     */
    @GetMapping("/queue")
    @Operation(summary = "分页查询待审核列表")
    @SecurityRequirement(name = "BearAuth")
    public Result<PageResult> pageQuery(@Validated ModerationQueuePageQueryDTO dto) {
        log.info("查询审核队列: page={}, pageSize={}, targetType={}", dto.getPage(), dto.getPageSize(), dto.getTargetType());
        PageResult pageResult = moderationQueueService.pageQuery(dto.getPage(), dto.getPageSize(), dto.getTargetType());
        return Result.success(pageResult);
    }

    /**
     * 查看审核详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查看审核详情")
    @SecurityRequirement(name = "BearAuth")
    public Result<ModerationQueueVO> getById(@PathVariable @NotNull @Min(1) Long id) {
        ModerationQueueVO vo = moderationQueueService.getById(id);
        return Result.success(vo);
    }

    /**
     * 执行审核操作
     */
    @PostMapping("/{id}/action")
    @Operation(summary = "执行审核操作（通过/驳回/下架/影子发布）")
    @SecurityRequirement(name = "BearAuth")
    public Result<Void> executeAction(@PathVariable @NotNull @Min(1) Long id,
                                      @RequestBody @Valid ModerationActionDTO dto) {
        log.info("执行审核操作: id={}, action={}, reason={}", id, dto.getAction(), dto.getReason());
        moderationQueueService.executeAction(id, dto.getAction(), dto.getReason());
        return Result.success();
    }
}
