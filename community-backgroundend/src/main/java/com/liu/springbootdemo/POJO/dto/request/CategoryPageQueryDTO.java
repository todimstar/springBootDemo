package com.liu.springbootdemo.POJO.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类分页查询请求体
 * 分类查询有附加条件->是管理员与否
 * 特色：用setPage等重写自动回正参数，用isAdmin为xml区分是否查询禁用分区
 */
@Data
public class CategoryPageQueryDTO {
    // 内部参数，从前端的Json中忽略
    @JsonIgnore
    private boolean isAdmin=false;  //默认false，用Boolean能允许null，且@Date生成Boolean的get方法为getIsAdmin()，生成boolean的get方法为isAdmin(),易用错

    @NotNull(message = "页码不能为空")
    private Integer page;

    @NotNull(message = "每页数量不能为空")
    private Integer pageSize;

    // 手动重写参数set，实现自动回正逻辑
    public void setPage(Integer page){
        // 空和过小情况
        if(page == null || page <1 ){
            this.page = 1;
        }else{
            this.page = page;
        }
    }

    public void setPageSize(Integer pageSize){
        // 空或过小
        if(pageSize == null || pageSize <1){
            this.pageSize = 10; //
        }// 请求单页过大
        else if(pageSize > 100){
            this.pageSize = 100;
        }else{
            this.pageSize = pageSize;
        }
    }

}
