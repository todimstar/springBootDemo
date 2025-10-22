package com.liu.springbootdemo.POJO.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建分区请求 DTO
 * 用于接收前端创建分区的请求参数
 */
@Data
public class CreateCategoryDTO {

    @NotBlank(message = "分区名称不能为空")
    @Size(max = 50, message = "分区名称不能超过50个字符")
    private String name;

    @Size(max = 200, message = "分区描述不能超过200个字符")
    private String description;

    private String icon;

    private Integer sortOrder = 0;  // 默认排序权重为0
}
