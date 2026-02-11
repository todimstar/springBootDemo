package com.liu.springbootdemo.POJO.dto.request;

import lombok.Data;

/**
 * 审核队列分页查询 DTO
 * 支持按内容类型筛选，按风险分降序排列
 */
@Data
public class ModerationQueuePageQueryDTO {

    private Integer page;
    private Integer pageSize;
    /** 内容类型筛选：post / comment，null 为全部 */
    private String targetType;

    public void setPage(Integer page) {
        if (page == null || page < 1) {
            this.page = 1;
        } else {
            this.page = page;
        }
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            this.pageSize = 10;
        } else if (pageSize > 100) {
            this.pageSize = 100;
        } else {
            this.pageSize = pageSize;
        }
    }
}
