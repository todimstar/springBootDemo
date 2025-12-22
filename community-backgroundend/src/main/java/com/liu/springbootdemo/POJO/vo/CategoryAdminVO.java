package com.liu.springbootdemo.POJO.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理分区 VO
 * 比用户多isActive属性
 * 法二:可以用@JsonView实现同样分权限显示
 */
@Data
public class CategoryAdminVO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer postCount;
    private Integer sortOrder;
    
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Boolean isActive;   //Boolean和boolean都一样，但由于转换前的category是Boolean，而BeanUntil不会自动封转所以得保持类型一致，否则会出现null
                                // 在这里，因为之后的BeanUntil的copy能正确浅拷贝String、Integer这些常量类，更能赋值复制long,int这些，不过为了统一和拓展性，用Boolean了
                                //之后只要注意不要将可变的引用类型加入就行如，如map,List之类的
}
