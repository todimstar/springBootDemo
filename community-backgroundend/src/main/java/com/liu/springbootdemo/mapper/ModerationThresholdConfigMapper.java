package com.liu.springbootdemo.mapper;

import com.liu.springbootdemo.POJO.entity.ModerationThresholdConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审核阈值配置 Mapper
 */
@Mapper
public interface ModerationThresholdConfigMapper {

    @Select("SELECT id, config_key, config_value, description, created_at, updated_at FROM moderation_threshold_config")
    List<ModerationThresholdConfig> findAll();

    @Select("SELECT id, config_key, config_value, description, created_at, updated_at FROM moderation_threshold_config WHERE config_key = #{configKey}")
    ModerationThresholdConfig findByKey(String configKey);
}
