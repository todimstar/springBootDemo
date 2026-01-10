package com.liu.springbootdemo.converter;

import com.liu.springbootdemo.POJO.dto.RegisterDTO;
import com.liu.springbootdemo.POJO.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConverter {
    /**
     * 将User 实体与 DTO/VO 之间的转换方法
     * 自动忽略掉DTO中的验证码字段
     */
    User registerDtoToUser(RegisterDTO registerDTO);

}
