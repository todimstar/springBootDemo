package com.liu.springbootdemo.mapper;

import com.github.pagehelper.Page;
import com.liu.springbootdemo.POJO.entity.ModerationRule;
import com.liu.springbootdemo.POJO.vo.ModerationRuleVO;
import org.apache.ibatis.annotations.*;

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

    /**
     * 分页查询规则（支持按类型和启用状态筛选）
     */
    Page<ModerationRuleVO> pageQuery(@Param("ruleType") String ruleType,
                                     @Param("enabled") Boolean enabled);

    /**
     * 根据 ID 查询未删除的规则
     */
    @Select("SELECT * FROM moderation_rule WHERE id = #{id} AND deleted = 0")
    ModerationRule findById(@Param("id") Long id);

    /**
     * 新增规则
     */
    @Insert("INSERT INTO moderation_rule (rule_type, pattern, weight_score, enabled, description) " +
            "VALUES (#{ruleType}, #{pattern}, #{weightScore}, #{enabled}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ModerationRule rule);

    /**
     * 更新规则
     */
    void update(ModerationRule rule);

    /**
     * 软删除规则
     */
    @Update("UPDATE moderation_rule SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDelete(@Param("id") Long id);
}
