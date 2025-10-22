package com.liu.springbootdemo.POJO.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新分区基本信息请求 DTO
 */
@Data
public class UpdateCategoryDTO {

    @NotNull(message = "分区ID不能为空")
    private Long id;

    @Size(max = 50, message = "分区名称不能超过50个字符")
    private String name;

    @Size(max = 200, message = "分区描述不能超过200个字符")
    private String description;

    private String icon;

    private Integer sortOrder;
}
