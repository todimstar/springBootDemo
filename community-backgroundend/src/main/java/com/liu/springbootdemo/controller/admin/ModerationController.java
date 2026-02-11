package com.liu.springbootdemo.controller.admin;

import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.Result.Result;
import com.liu.springbootdemo.POJO.dto.request.ModerationActionDTO;
import com.liu.springbootdemo.POJO.dto.request.ModerationQueuePageQueryDTO;
import com.liu.springbootdemo.POJO.dto.request.ModerationRuleCreateDTO;
import com.liu.springbootdemo.POJO.dto.request.ModerationRulePageQueryDTO;
import com.liu.springbootdemo.POJO.dto.request.ModerationRuleUpdateDTO;
import com.liu.springbootdemo.POJO.vo.ModerationAuditLogVO;
import com.liu.springbootdemo.POJO.vo.ModerationQueueVO;
import com.liu.springbootdemo.POJO.vo.ModerationRuleVO;
import com.liu.springbootdemo.service.ModerationAuditLogService;
import com.liu.springbootdemo.service.ModerationQueueService;
import com.liu.springbootdemo.service.ModerationRuleService;
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

import java.util.List;

@RestController("adminModerationController")
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("api/admin/moderation")
@Validated
@Slf4j
@Tag(name = "管理员审核管理接口", description = "审核队列查询与操作")
public class ModerationController {

    @Autowired
    private ModerationQueueService moderationQueueService;

    @Autowired
    private ModerationAuditLogService moderationAuditLogService;

    @Autowired
    private ModerationRuleService moderationRuleService;

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

    /**
     * 查询审计轨迹
     */
    @GetMapping("/{id}/logs")
    @Operation(summary = "查询审核审计轨迹")
    @SecurityRequirement(name = "BearAuth")
    public Result<List<ModerationAuditLogVO>> getAuditLogs(@PathVariable @NotNull @Min(1) Long id) {
        // 先查队列记录以获取 targetId 和 targetType
        ModerationQueueVO queueItem = moderationQueueService.getById(id);
        List<ModerationAuditLogVO> logs = moderationAuditLogService.getAuditLogs(
                queueItem.getTargetId(), queueItem.getTargetType());
        return Result.success(logs);
    }

    // ==================== 规则管理接口 ====================

    /**
     * 分页查询规则列表
     */
    @GetMapping("/rules")
    @Operation(summary = "分页查询审核规则列表")
    @SecurityRequirement(name = "BearAuth")
    public Result<PageResult> pageQueryRules(@Validated ModerationRulePageQueryDTO dto) {
        log.info("查询规则列表: page={}, pageSize={}, ruleType={}, enabled={}",
                dto.getPage(), dto.getPageSize(), dto.getRuleType(), dto.getEnabled());
        PageResult pageResult = moderationRuleService.pageQuery(
                dto.getPage(), dto.getPageSize(), dto.getRuleType(), dto.getEnabled());
        return Result.success(pageResult);
    }

    /**
     * 新增规则
     */
    @PostMapping("/rules")
    @Operation(summary = "新增审核规则")
    @SecurityRequirement(name = "BearAuth")
    public Result<ModerationRuleVO> createRule(@RequestBody @Valid ModerationRuleCreateDTO dto) {
        log.info("新增审核规则: type={}, pattern={}, weight={}", dto.getRuleType(), dto.getPattern(), dto.getWeightScore());
        ModerationRuleVO vo = moderationRuleService.createRule(
                dto.getRuleType(), dto.getPattern(), dto.getWeightScore(),
                dto.getEnabled() != null ? dto.getEnabled() : true, dto.getDescription());
        return Result.success(vo);
    }

    /**
     * 修改规则
     */
    @PutMapping("/rules/{id}")
    @Operation(summary = "修改审核规则")
    @SecurityRequirement(name = "BearAuth")
    public Result<ModerationRuleVO> updateRule(@PathVariable @NotNull @Min(1) Long id,
                                               @RequestBody @Valid ModerationRuleUpdateDTO dto) {
        log.info("修改审核规则: id={}", id);
        ModerationRuleVO vo = moderationRuleService.updateRule(
                id, dto.getRuleType(), dto.getPattern(),
                dto.getWeightScore(), dto.getEnabled(), dto.getDescription());
        return Result.success(vo);
    }

    /**
     * 软删除规则
     */
    @DeleteMapping("/rules/{id}")
    @Operation(summary = "软删除审核规则")
    @SecurityRequirement(name = "BearAuth")
    public Result<Void> deleteRule(@PathVariable @NotNull @Min(1) Long id) {
        log.info("删除审核规则: id={}", id);
        moderationRuleService.deleteRule(id);
        return Result.success();
    }
}
