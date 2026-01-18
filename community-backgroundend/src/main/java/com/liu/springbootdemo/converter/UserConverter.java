package com.liu.springbootdemo.converter;

import com.liu.springbootdemo.POJO.dto.user.RegisterDTO;
import com.liu.springbootdemo.POJO.dto.user.UpdateUserDTO;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.vo.UpdateUserVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConverter {
    /**
     * 将User 实体与 DTO/VO 之间的转换方法
     * 自动忽略掉DTO中的验证码字段
     */
    User registerDtoToUser(RegisterDTO registerDTO);

    /**
     * UpdateDTO转User实体
     */
    User UpdateDtoTOUser(UpdateUserDTO updateUserDTO);

    /**
     * user转UpdateVO
     * 数据库查出来转更新VO
     */
    UpdateUserVO ToUpdateVO(User user);

}
