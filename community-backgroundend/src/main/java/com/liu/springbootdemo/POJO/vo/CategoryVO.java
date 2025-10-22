package com.liu.springbootdemo.POJO.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分区 VO - 返回给前端的视图对象
 * 只包含前端需要展示的字段
 */
@Data
public class CategoryVO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer postCount;
    private Integer sortOrder;
    private LocalDateTime createTime;

    // 注意：不包含 isActive（内部字段，前端不需要）
}
