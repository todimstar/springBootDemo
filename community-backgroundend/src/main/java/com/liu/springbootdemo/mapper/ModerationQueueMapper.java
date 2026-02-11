package com.liu.springbootdemo.mapper;

import com.github.pagehelper.Page;
import com.liu.springbootdemo.POJO.entity.ModerationQueue;
import com.liu.springbootdemo.POJO.vo.ModerationQueueVO;
import org.apache.ibatis.annotations.*;

/**
 * 审核队列 Mapper
 */
@Mapper
public interface ModerationQueueMapper {

    @Insert("INSERT INTO moderation_queue (target_id, target_type, author_id, content_summary, risk_score, hit_rules, status) " +
            "VALUES (#{targetId}, #{targetType}, #{authorId}, #{contentSummary}, #{riskScore}, #{hitRules}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ModerationQueue queue);

    /**
     * 分页查询审核队列（支持 targetType 筛选），按风险分降序
     */
    Page<ModerationQueueVO> pageQuery(@Param("targetType") String targetType);

    /**
     * 根据 ID 查询审核队列记录（关联用户名）
     */
    @Select("SELECT mq.*, u.username AS author_name FROM moderation_queue mq " +
            "LEFT JOIN users u ON mq.author_id = u.id WHERE mq.id = #{id}")
    ModerationQueueVO findById(Long id);

    /**
     * 更新审核队列状态
     */
    @Update("UPDATE moderation_queue SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
