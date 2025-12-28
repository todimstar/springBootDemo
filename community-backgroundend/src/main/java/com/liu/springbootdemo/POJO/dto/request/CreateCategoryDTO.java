package com.liu.springbootdemo.POJO.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建分区请求 DTO
 * 用于接收前端创建分区的请求参数
 */
@Data
@Schema(description = "创建分区请求 DTO")
public class CreateCategoryDTO {

    @NotBlank(message = "分区名称不能为空")
    @Size(max = 50, message = "分区名称不能超过{max}个字符")
    @Schema(description = "请求的分区名称", example = "技术交流")
    private String name;

    @Size(max = 200, message = "分区描述不能超过{max}个字符")
    @Schema(description = "请求的分区描述", example = "讨论各种技术相关的话题")
    private String description;

    @Schema(description = "等文件上传接口做好就知道怎么做了", example = "/images/icons/tech.png")
    private String icon;

    @Schema(description = "分区排序权重，越大越靠前，默认0", example = "0")
    private Integer sortOrder = 0;  // 默认排序权重为0
}
