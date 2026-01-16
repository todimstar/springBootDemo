package com.liu.springbootdemo.POJO.dto.user.admin;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateUserByAdminDTO {

    @NotNull(message = "要修改的用户ID不能为空")
    private Long id;
    private String username;    //用户名
    //密码顶多设个重置默认密码的接口，也不会给到管理员去改的
    private String role;    //用户角色
    private String avatarUrl;  //头像url
    @Min(value = 0,message = "超过目前的性别类型啦~")
    @Max(value = 2,message = "超过目前的性别类型啦~")
    private int gender; //0保密，1男，2女
    @Size(max = 500,message = "您这简介也忒长了，小作文啊？{max}字简介~")
    private String bio; //个人简介
    @Size(max = 100,message = "这是长达{max}字的未来领土城市吗？")
    private String location;    //所在地，城市
    private int points; //积分
    private int level;  //用户等级
    private boolean isBanned;  //是否被封禁
    @Size(max = 255,message = "阿巴阿巴，有点长了{max}字原因")
    private String banReason;   //封禁原因
    private LocalDateTime banUntil;    //封禁截止时间
}
