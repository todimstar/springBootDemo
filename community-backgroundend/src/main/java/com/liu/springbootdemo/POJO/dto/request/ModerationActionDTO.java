package com.liu.springbootdemo.POJO.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核操作请求 DTO
 */
@Data
public class ModerationActionDTO {
    /** 操作类型：approve / reject / takedown / shadow_ban */
    @NotBlank(message = "操作类型不能为空")
    private String action;

    /** 审核理由 */
    @NotBlank(message = "审核理由不能为空")
    private String reason;
}
