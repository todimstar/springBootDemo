package com.liu.springbootdemo.service.impl;

import com.github.pagehelper.Page;
import com.liu.springbootdemo.POJO.vo.ModerationQueueVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.enums.ModerationAction;
import com.liu.springbootdemo.common.enums.PostStatus;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.mapper.ModerationQueueMapper;
import com.liu.springbootdemo.mapper.PostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 审核队列服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ModerationQueueServiceImplTest {

    @Mock
    private ModerationQueueMapper moderationQueueMapper;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private ModerationQueueServiceImpl moderationQueueService;

    // ============ 分页查询测试 ============

    @Nested
    class PageQueryTests {

        @Test
        void pageQuery_noFilter_returnsAll() {
            Page<ModerationQueueVO> mockPage = new Page<>(1, 10);
            mockPage.setTotal(2);
            ModerationQueueVO vo1 = createQueueVO(1L, "post", 80);
            ModerationQueueVO vo2 = createQueueVO(2L, "comment", 50);
            mockPage.add(vo1);
            mockPage.add(vo2);
            when(moderationQueueMapper.pageQuery(isNull())).thenReturn(mockPage);

            var result = moderationQueueService.pageQuery(1, 10, null);

            assertEquals(2L, result.getTotal());
            assertEquals(2, result.getResults().size());
        }

        @Test
        void pageQuery_filterByPost_returnsPostsOnly() {
            Page<ModerationQueueVO> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.add(createQueueVO(1L, "post", 60));
            when(moderationQueueMapper.pageQuery("post")).thenReturn(mockPage);

            var result = moderationQueueService.pageQuery(1, 10, "post");

            assertEquals(1L, result.getTotal());
        }

        @Test
        void pageQuery_filterByComment_returnsCommentsOnly() {
            Page<ModerationQueueVO> mockPage = new Page<>(1, 10);
            mockPage.setTotal(0);
            when(moderationQueueMapper.pageQuery("comment")).thenReturn(mockPage);

            var result = moderationQueueService.pageQuery(1, 10, "comment");

            assertEquals(0L, result.getTotal());
        }
    }

    // ============ 查询详情测试 ============

    @Nested
    class GetByIdTests {

        @Test
        void getById_exists_returnsVO() {
            ModerationQueueVO vo = createQueueVO(1L, "post", 55);
            when(moderationQueueMapper.findById(1L)).thenReturn(vo);

            ModerationQueueVO result = moderationQueueService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(55, result.getRiskScore());
        }

        @Test
        void getById_notFound_throwsException() {
            when(moderationQueueMapper.findById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> moderationQueueService.getById(999L));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
        }
    }

    // ============ 审核操作测试 ============

    @Nested
    class ExecuteActionTests {

        private ModerationQueueVO pendingItem;

        @BeforeEach
        void setUp() {
            pendingItem = createQueueVO(1L, "post", 50);
            pendingItem.setTargetId(100L);
            pendingItem.setStatus("pending");
        }

        @Test
        void executeAction_approve_updatesPostToPublished() {
            when(moderationQueueMapper.findById(1L)).thenReturn(pendingItem);
            when(postMapper.isExistById(100L)).thenReturn(true);

            moderationQueueService.executeAction(1L, "approve", "内容正常");

            verify(postMapper).updateStatus(100L, PostStatus.PUBLISHED.getStatus());
            verify(moderationQueueMapper).updateStatus(1L, "reviewed");
        }

        @Test
        void executeAction_reject_updatesPostToRejected() {
            when(moderationQueueMapper.findById(1L)).thenReturn(pendingItem);
            when(postMapper.isExistById(100L)).thenReturn(true);

            moderationQueueService.executeAction(1L, "reject", "违规内容");

            verify(postMapper).updateStatus(100L, PostStatus.REJECTED.getStatus());
            verify(moderationQueueMapper).updateStatus(1L, "reviewed");
        }

        @Test
        void executeAction_takedown_updatesPostToDeleted() {
            when(moderationQueueMapper.findById(1L)).thenReturn(pendingItem);
            when(postMapper.isExistById(100L)).thenReturn(true);

            moderationQueueService.executeAction(1L, "takedown", "严重违规需下架");

            verify(postMapper).updateStatus(100L, PostStatus.DELETED.getStatus());
            verify(moderationQueueMapper).updateStatus(1L, "reviewed");
        }

        @Test
        void executeAction_shadowBan_updatesPostToShadowBanned() {
            when(moderationQueueMapper.findById(1L)).thenReturn(pendingItem);
            when(postMapper.isExistById(100L)).thenReturn(true);

            moderationQueueService.executeAction(1L, "shadow_ban", "隐蔽处理");

            verify(postMapper).updateStatus(100L, PostStatus.SHADOW_BANNED.getStatus());
            verify(moderationQueueMapper).updateStatus(1L, "reviewed");
        }

        @Test
        void executeAction_invalidAction_throwsException() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> moderationQueueService.executeAction(1L, "invalid_action", "理由"));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
        }

        @Test
        void executeAction_queueNotFound_throwsException() {
            when(moderationQueueMapper.findById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> moderationQueueService.executeAction(999L, "approve", "通过"));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
        }

        @Test
        void executeAction_alreadyReviewed_idempotent() {
            ModerationQueueVO reviewedItem = createQueueVO(1L, "post", 50);
            reviewedItem.setStatus("reviewed");
            when(moderationQueueMapper.findById(1L)).thenReturn(reviewedItem);

            // 幂等：不应抛出异常，也不应重复操作
            moderationQueueService.executeAction(1L, "approve", "通过");

            verify(postMapper, never()).updateStatus(anyLong(), anyInt());
            verify(moderationQueueMapper, never()).updateStatus(anyLong(), anyString());
        }

        @Test
        void executeAction_postNotFound_throwsException() {
            when(moderationQueueMapper.findById(1L)).thenReturn(pendingItem);
            when(postMapper.isExistById(100L)).thenReturn(false);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> moderationQueueService.executeAction(1L, "approve", "通过"));
            assertEquals(ErrorCode.POST_NOT_FOUND.getCode(), ex.getCode());
        }
    }

    // ============ 辅助方法 ============

    private ModerationQueueVO createQueueVO(Long id, String targetType, int riskScore) {
        ModerationQueueVO vo = new ModerationQueueVO();
        vo.setId(id);
        vo.setTargetId(id * 100);
        vo.setTargetType(targetType);
        vo.setAuthorId(1L);
        vo.setAuthorName("testuser");
        vo.setContentSummary("测试内容摘要");
        vo.setRiskScore(riskScore);
        vo.setHitRules("[{\"ruleId\":1,\"ruleType\":\"keyword\",\"hitSnippet\":\"敏感词\",\"weightScore\":50}]");
        vo.setStatus("pending");
        vo.setCreatedAt(LocalDateTime.now());
        vo.setUpdatedAt(LocalDateTime.now());
        return vo;
    }
}
