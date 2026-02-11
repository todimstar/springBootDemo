package com.liu.springbootdemo.mapper;

import com.liu.springbootdemo.POJO.entity.ModerationRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审核规则 Mapper
 */
@Mapper
public interface ModerationRuleMapper {

    /**
     * 查询所有启用且未删除的规则
     */
    @Select("SELECT * FROM moderation_rule WHERE enabled = 1 AND deleted = 0")
    List<ModerationRule> findAllEnabled();
}
