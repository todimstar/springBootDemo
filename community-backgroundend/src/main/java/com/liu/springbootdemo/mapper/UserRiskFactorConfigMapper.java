package com.liu.springbootdemo.mapper;

import com.liu.springbootdemo.POJO.entity.UserRiskFactorConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户风险因子加权配置 Mapper
 */
@Mapper
public interface UserRiskFactorConfigMapper {

    /**
     * 查询所有启用的加权配置
     */
    @Select("SELECT * FROM user_risk_factor_config WHERE enabled = 1")
    List<UserRiskFactorConfig> findAllEnabled();
}
