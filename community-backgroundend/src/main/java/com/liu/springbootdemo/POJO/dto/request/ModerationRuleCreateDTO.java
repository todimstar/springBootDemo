package com.liu.springbootdemo.POJO.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增审核规则 DTO
 */
@Data
public class ModerationRuleCreateDTO {

    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    @NotBlank(message = "匹配模式不能为空")
    private String pattern;

    @NotNull(message = "权重分不能为空")
    @Min(value = 0, message = "权重分最小为0")
    @Max(value = 100, message = "权重分最大为100")
    private Integer weightScore;

    private Boolean enabled = true;

    private String description;
}
