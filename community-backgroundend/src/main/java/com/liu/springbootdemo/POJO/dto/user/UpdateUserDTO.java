package com.liu.springbootdemo.POJO.dto.user;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户改自己信息的请求体
 */
@Data
public class UpdateUserDTO {
    //id由Security提供
    private String username;    //用户名
//    private String email;   //邮箱作为唯一要跟密码一样单独验证后修改，走独立接口
//    private String role;    //用户角色，在adminDTO中有
    private String avatarUrl;  //头像url
    @Min(value = 0,message = "超过目前的性别类型啦~")
    @Max(value = 2,message = "超过目前的性别类型啦~")
    private int gender; //0保密，1男，2女
    @Size(max = 500,message = "您这简介也忒长了，小作文啊？{max}字简介~")
    private String bio; //个人简介
    @Size(max = 100,message = "这是长达{max}字的未来领土城市吗？")
    private String location;    //所在地，城市
}
