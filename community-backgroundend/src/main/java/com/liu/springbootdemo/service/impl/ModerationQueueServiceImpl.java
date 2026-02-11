package com.liu.springbootdemo.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.vo.ModerationQueueVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.enums.ModerationAction;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.mapper.ModerationQueueMapper;
import com.liu.springbootdemo.mapper.PostMapper;
import com.liu.springbootdemo.service.ModerationQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审核队列服务实现
 */
@Service
@Slf4j
public class ModerationQueueServiceImpl implements ModerationQueueService {

    @Autowired
    private ModerationQueueMapper moderationQueueMapper;

    @Autowired
    private PostMapper postMapper;

    @Override
    public PageResult pageQuery(Integer page, Integer pageSize, String targetType) {
        PageHelper.startPage(page, pageSize);
        Page<ModerationQueueVO> voPage = moderationQueueMapper.pageQuery(targetType);
        return new PageResult(voPage.getTotal(), voPage.getResult());
    }

    @Override
    public ModerationQueueVO getById(Long id) {
        ModerationQueueVO vo = moderationQueueMapper.findById(id);
        if (vo == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "审核记录不存在");
        }
        return vo;
    }

    @Override
    @Transactional
    public void executeAction(Long id, String action, String reason) {
        // 1. 校验操作类型
        ModerationAction moderationAction = ModerationAction.fromCode(action);
        if (moderationAction == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "非法操作类型: " + action);
        }

        // 2. 查询审核队列记录
        ModerationQueueVO queueItem = moderationQueueMapper.findById(id);
        if (queueItem == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "审核记录不存在");
        }

        // 3. 幂等检查：已审核的记录不重复处理
        if ("reviewed".equals(queueItem.getStatus())) {
            log.info("审核记录已处理，跳过重复操作: id={}, action={}", id, action);
            return;
        }

        // 4. 更新帖子/评论状态
        if ("post".equals(queueItem.getTargetType())) {
            if (!postMapper.isExistById(queueItem.getTargetId())) {
                throw new BusinessException(ErrorCode.POST_NOT_FOUND, "关联帖子不存在");
            }
            postMapper.updateStatus(queueItem.getTargetId(), moderationAction.getTargetPostStatus());
            log.info("帖子状态已更新: postId={}, newStatus={}", queueItem.getTargetId(), moderationAction.getTargetPostStatus());
        }

        // 5. 更新队列状态
        moderationQueueMapper.updateStatus(id, "reviewed");

        log.info("审核操作完成: queueId={}, action={}, reason={}", id, action, reason);
    }
}
