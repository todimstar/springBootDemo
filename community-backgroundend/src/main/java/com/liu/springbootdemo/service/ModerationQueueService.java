package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.vo.ModerationQueueVO;

/**
 * 审核队列服务接口
 */
public interface ModerationQueueService {

    /**
     * 分页查询待审核列表
     *
     * @param page       页码
     * @param pageSize   每页数量
     * @param targetType 内容类型筛选（post/comment），null 为全部
     * @return 分页结果
     */
    PageResult pageQuery(Integer page, Integer pageSize, String targetType);

    /**
     * 查看审核详情
     *
     * @param id 审核队列 ID
     * @return 审核队列详情
     */
    ModerationQueueVO getById(Long id);

    /**
     * 执行审核操作
     *
     * @param id     审核队列 ID
     * @param action 操作类型（approve/reject/takedown/shadow_ban）
     * @param reason 审核理由
     */
    void executeAction(Long id, String action, String reason);
}
