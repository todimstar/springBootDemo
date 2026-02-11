package com.liu.springbootdemo.POJO.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 修改审核规则 DTO
 */
@Data
public class ModerationRuleUpdateDTO {

    private String ruleType;

    private String pattern;

    @Min(value = 0, message = "权重分最小为0")
    @Max(value = 100, message = "权重分最大为100")
    private Integer weightScore;

    private Boolean enabled;

    private String description;
}
