package com.liu.springbootdemo.POJO.dto.request;

import lombok.Data;

/**
 * 审核规则分页查询 DTO
 */
@Data
public class ModerationRulePageQueryDTO {

    private Integer page;
    private Integer pageSize;
    /** 规则类型筛选：keyword / regex / blacklist，null 为全部 */
    private String ruleType;
    /** 启用状态筛选：true/false，null 为全部 */
    private Boolean enabled;

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
