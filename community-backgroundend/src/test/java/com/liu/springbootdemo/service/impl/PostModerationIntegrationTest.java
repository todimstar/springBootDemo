package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.dto.CreatePostDTO;
import com.liu.springbootdemo.POJO.dto.ModerationDecisionResult;
import com.liu.springbootdemo.POJO.dto.RiskScoreResult;
import com.liu.springbootdemo.POJO.entity.Post;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.vo.PostDetailVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.enums.ModerationDecision;
import com.liu.springbootdemo.common.enums.PostStatus;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.converter.PostConverter;
import com.liu.springbootdemo.mapper.CategoryMapper;
import com.liu.springbootdemo.mapper.PostMapper;
import com.liu.springbootdemo.service.CategoryService;
import com.liu.springbootdemo.service.ModerationDecisionService;
import com.liu.springbootdemo.service.UserService;
import com.liu.springbootdemo.common.utils.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 帖子接入审核链路集成测试
 * 覆盖场景：创建帖子三种决策路径、编辑帖子重新审核、被驳回帖子可重新编辑
 */
@ExtendWith(MockitoExtension.class)
class PostModerationIntegrationTest {

    @Mock
    private PostMapper postMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private UserService userService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private PostConverter postConverter;
    @Mock
    private ModerationDecisionService moderationDecisionService;

    @InjectMocks
    private PostServiceImpl postService;

    private User currentUser;
    private CreatePostDTO createPostDTO;
    private Post postInDb;
    private PostDetailVO postDetailVO;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");
        currentUser.setRole("user");

        createPostDTO = new CreatePostDTO();
        createPostDTO.setTitle("测试标题");
        createPostDTO.setContent("测试内容");
        createPostDTO.setSummary("测试摘要");
        createPostDTO.setCategoryId(1L);

        postInDb = new Post();
        postInDb.setId(100L);
        postInDb.setUserId(1L);
        postInDb.setTitle("测试标题");
        postInDb.setContent("测试内容");
        postInDb.setCategoryId(1L);
        postInDb.setCategoryName("测试分区");
        postInDb.setStatus(PostStatus.PUBLISHED.getStatus());

        postDetailVO = new PostDetailVO();
        postDetailVO.setId(100L);
        postDetailVO.setTitle("测试标题");
        postDetailVO.setStatus(PostStatus.PUBLISHED.getStatus());
    }

    // ==================== 创建帖子 - 审核链路测试 ====================

    @Nested
    @DisplayName("创建帖子 - 审核链路")
    class CreatePostModerationTests {

        @Test
        @DisplayName("自动放行：低风险内容自动发布，用户无感知")
        void createPost_autoApprove_shouldPublishDirectly() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
                lenient().when(categoryMapper.isActiveById(1L)).thenReturn(true);
                lenient().when(categoryMapper.findNameById(1L)).thenReturn("测试分区");
                doAnswer(inv -> { inv.getArgument(0, Post.class).setId(100L); return 1; })
                        .when(postMapper).insert(any(Post.class));

                RiskScoreResult safeResult = RiskScoreResult.safe();
                ModerationDecisionResult approveResult = new ModerationDecisionResult(
                        10, ModerationDecision.AUTO_APPROVE, PostStatus.PUBLISHED.getStatus(), safeResult);
                when(moderationDecisionService.decide(anyString(), eq(currentUser), eq(100L), eq("post")))
                        .thenReturn(approveResult);

                when(postMapper.updateStatus(eq(100L), eq(PostStatus.PUBLISHED.getStatus()))).thenReturn(1);
                when(postMapper.findById(100L)).thenReturn(postInDb);
                when(postConverter.toDetailVO(any(Post.class))).thenReturn(postDetailVO);

                PostDetailVO result = postService.createPost(createPostDTO);

                assertNotNull(result);
                verify(moderationDecisionService).decide(anyString(), eq(currentUser), eq(100L), eq("post"));
                verify(postMapper).updateStatus(eq(100L), eq(PostStatus.PUBLISHED.getStatus()));
            }
        }

        @Test
        @DisplayName("送人审：灰区内容进入待审核状态，用户正常收到响应")
        void createPost_pendingReview_shouldSetPendingStatus() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
                lenient().when(categoryMapper.isActiveById(1L)).thenReturn(true);
                lenient().when(categoryMapper.findNameById(1L)).thenReturn("测试分区");
                doAnswer(inv -> { inv.getArgument(0, Post.class).setId(100L); return 1; })
                        .when(postMapper).insert(any(Post.class));

                RiskScoreResult midResult = new RiskScoreResult(50, new ArrayList<>());
                ModerationDecisionResult pendingResult = new ModerationDecisionResult(
                        50, ModerationDecision.PENDING_REVIEW, PostStatus.PENDING_REVIEW.getStatus(), midResult);
                when(moderationDecisionService.decide(anyString(), eq(currentUser), eq(100L), eq("post")))
                        .thenReturn(pendingResult);

                Post pendingPost = new Post();
                pendingPost.setId(100L);
                pendingPost.setTitle("测试标题");
                pendingPost.setStatus(PostStatus.PENDING_REVIEW.getStatus());

                when(postMapper.updateStatus(eq(100L), eq(PostStatus.PENDING_REVIEW.getStatus()))).thenReturn(1);
                when(postMapper.findById(100L)).thenReturn(pendingPost);
                PostDetailVO pendingVO = new PostDetailVO();
                pendingVO.setId(100L);
                pendingVO.setStatus(PostStatus.PENDING_REVIEW.getStatus());
                when(postConverter.toDetailVO(any(Post.class))).thenReturn(pendingVO);

                PostDetailVO result = postService.createPost(createPostDTO);

                assertNotNull(result);
                verify(postMapper).updateStatus(eq(100L), eq(PostStatus.PENDING_REVIEW.getStatus()));
            }
        }

        @Test
        @DisplayName("自动拦截：高风险内容被拦截，返回友好提示")
        void createPost_autoReject_shouldThrowFriendlyException() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
                lenient().when(categoryMapper.isActiveById(1L)).thenReturn(true);
                lenient().when(categoryMapper.findNameById(1L)).thenReturn("测试分区");
                doAnswer(inv -> { inv.getArgument(0, Post.class).setId(100L); return 1; })
                        .when(postMapper).insert(any(Post.class));

                RiskScoreResult highResult = new RiskScoreResult(85, new ArrayList<>());
                ModerationDecisionResult rejectResult = new ModerationDecisionResult(
                        85, ModerationDecision.AUTO_REJECT, PostStatus.REJECTED.getStatus(), highResult);
                when(moderationDecisionService.decide(anyString(), eq(currentUser), eq(100L), eq("post")))
                        .thenReturn(rejectResult);
                when(postMapper.updateStatus(eq(100L), eq(PostStatus.REJECTED.getStatus()))).thenReturn(1);

                BusinessException ex = assertThrows(BusinessException.class,
                        () -> postService.createPost(createPostDTO));

                assertEquals(ErrorCode.POST_CONTENT_REJECTED.getCode(), ex.getCode());
                verify(postMapper).updateStatus(eq(100L), eq(PostStatus.REJECTED.getStatus()));
            }
        }

        @Test
        @DisplayName("审核引擎接收的文本为 title + content 拼接")
        void createPost_shouldPassConcatenatedTextToModeration() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
                lenient().when(categoryMapper.isActiveById(1L)).thenReturn(true);
                lenient().when(categoryMapper.findNameById(1L)).thenReturn("测试分区");
                doAnswer(inv -> { inv.getArgument(0, Post.class).setId(100L); return 1; })
                        .when(postMapper).insert(any(Post.class));

                RiskScoreResult safeResult = RiskScoreResult.safe();
                ModerationDecisionResult approveResult = new ModerationDecisionResult(
                        0, ModerationDecision.AUTO_APPROVE, PostStatus.PUBLISHED.getStatus(), safeResult);
                when(moderationDecisionService.decide(anyString(), eq(currentUser), eq(100L), eq("post")))
                        .thenReturn(approveResult);
                when(postMapper.updateStatus(eq(100L), anyInt())).thenReturn(1);
                when(postMapper.findById(100L)).thenReturn(postInDb);
                when(postConverter.toDetailVO(any(Post.class))).thenReturn(postDetailVO);

                postService.createPost(createPostDTO);

                ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
                verify(moderationDecisionService).decide(textCaptor.capture(), eq(currentUser), eq(100L), eq("post"));
                String moderatedText = textCaptor.getValue();
                assertTrue(moderatedText.contains("测试标题"));
                assertTrue(moderatedText.contains("测试内容"));
            }
        }
    }

    // ==================== 编辑帖子 - 审核链路测试 ====================

    @Nested
    @DisplayName("编辑帖子 - 审核链路")
    class UpdatePostModerationTests {

        @Test
        @DisplayName("内容变更触发重新审核 - 自动放行")
        void updatePost_contentChanged_autoApprove() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
                when(postMapper.findById(100L)).thenReturn(postInDb);

                Post updateData = new Post();
                updateData.setContent("修改后的正常内容");
                when(postMapper.updatePost(eq(100L), any(Post.class))).thenReturn(1);

                Post updatedPost = new Post();
                updatedPost.setId(100L);
                updatedPost.setTitle("测试标题");
                updatedPost.setContent("修改后的正常内容");
                // findById 第一次返回 postInDb，第二次返回 updatedPost（审核后再查一次）
                when(postMapper.findById(100L)).thenReturn(postInDb).thenReturn(updatedPost).thenReturn(updatedPost);

                RiskScoreResult safeResult = RiskScoreResult.safe();
                ModerationDecisionResult approveResult = new ModerationDecisionResult(
                        5, ModerationDecision.AUTO_APPROVE, PostStatus.PUBLISHED.getStatus(), safeResult);
                when(moderationDecisionService.decide(anyString(), eq(currentUser), eq(100L), eq("post")))
                        .thenReturn(approveResult);
                when(postMapper.updateStatus(100L, PostStatus.PUBLISHED.getStatus())).thenReturn(1);
                when(postConverter.toDetailVO(any(Post.class))).thenReturn(postDetailVO);

                PostDetailVO result = postService.updatePost(100L, updateData);

                assertNotNull(result);
                verify(moderationDecisionService).decide(anyString(), eq(currentUser), eq(100L), eq("post"));
                verify(postMapper).updateStatus(100L, PostStatus.PUBLISHED.getStatus());
            }
        }

        @Test
        @DisplayName("内容变更触发重新审核 - 自动拦截返回友好提示")
        void updatePost_contentChanged_autoReject_shouldThrow() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
                when(postMapper.findById(100L)).thenReturn(postInDb);

                Post updateData = new Post();
                updateData.setContent("违规内容测试");
                when(postMapper.updatePost(eq(100L), any(Post.class))).thenReturn(1);

                Post updatedPost = new Post();
                updatedPost.setId(100L);
                updatedPost.setTitle("测试标题");
                updatedPost.setContent("违规内容测试");
                when(postMapper.findById(100L)).thenReturn(postInDb).thenReturn(updatedPost);

                RiskScoreResult highResult = new RiskScoreResult(90, new ArrayList<>());
                ModerationDecisionResult rejectResult = new ModerationDecisionResult(
                        90, ModerationDecision.AUTO_REJECT, PostStatus.REJECTED.getStatus(), highResult);
                when(moderationDecisionService.decide(anyString(), eq(currentUser), eq(100L), eq("post")))
                        .thenReturn(rejectResult);
                when(postMapper.updateStatus(100L, PostStatus.REJECTED.getStatus())).thenReturn(1);

                BusinessException ex = assertThrows(BusinessException.class,
                        () -> postService.updatePost(100L, updateData));

                assertEquals(ErrorCode.POST_CONTENT_REJECTED.getCode(), ex.getCode());
                verify(postMapper).updateStatus(100L, PostStatus.REJECTED.getStatus());
            }
        }

        @Test
        @DisplayName("内容变更触发重新审核 - 送人审")
        void updatePost_contentChanged_pendingReview() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
                when(postMapper.findById(100L)).thenReturn(postInDb);

                Post updateData = new Post();
                updateData.setTitle("修改后的灰区标题");
                when(postMapper.updatePost(eq(100L), any(Post.class))).thenReturn(1);

                Post updatedPost = new Post();
                updatedPost.setId(100L);
                updatedPost.setTitle("修改后的灰区标题");
                updatedPost.setContent("测试内容");
                when(postMapper.findById(100L)).thenReturn(postInDb).thenReturn(updatedPost).thenReturn(updatedPost);

                RiskScoreResult midResult = new RiskScoreResult(45, new ArrayList<>());
                ModerationDecisionResult pendingResult = new ModerationDecisionResult(
                        45, ModerationDecision.PENDING_REVIEW, PostStatus.PENDING_REVIEW.getStatus(), midResult);
                when(moderationDecisionService.decide(anyString(), eq(currentUser), eq(100L), eq("post")))
                        .thenReturn(pendingResult);
                when(postMapper.updateStatus(100L, PostStatus.PENDING_REVIEW.getStatus())).thenReturn(1);
                PostDetailVO pendingVO = new PostDetailVO();
                pendingVO.setId(100L);
                pendingVO.setStatus(PostStatus.PENDING_REVIEW.getStatus());
                when(postConverter.toDetailVO(any(Post.class))).thenReturn(pendingVO);

                PostDetailVO result = postService.updatePost(100L, updateData);

                assertNotNull(result);
                verify(moderationDecisionService).decide(anyString(), eq(currentUser), eq(100L), eq("post"));
                verify(postMapper).updateStatus(100L, PostStatus.PENDING_REVIEW.getStatus());
            }
        }

        @Test
        @DisplayName("非内容变更（如仅改封面图）不触发审核")
        void updatePost_nonContentChange_shouldSkipModeration() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
                when(postMapper.findById(100L)).thenReturn(postInDb);

                Post updateData = new Post();
                updateData.setCoverImage("https://example.com/new-image.jpg");
                when(postMapper.updatePost(eq(100L), any(Post.class))).thenReturn(1);
                when(postConverter.toDetailVO(any(Post.class))).thenReturn(postDetailVO);

                PostDetailVO result = postService.updatePost(100L, updateData);

                assertNotNull(result);
                verify(moderationDecisionService, never()).decide(anyString(), any(User.class), anyLong(), anyString());
            }
        }

        @Test
        @DisplayName("被驳回的帖子可以编辑后重新提交审核")
        void updatePost_rejectedPost_canBeEditedAndResubmitted() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);

                Post rejectedPost = new Post();
                rejectedPost.setId(100L);
                rejectedPost.setUserId(1L);
                rejectedPost.setTitle("被驳回的帖子");
                rejectedPost.setContent("原始内容");
                rejectedPost.setStatus(PostStatus.REJECTED.getStatus());

                when(postMapper.findById(100L)).thenReturn(rejectedPost);

                Post updateData = new Post();
                updateData.setContent("修改后的合规内容");
                when(postMapper.updatePost(eq(100L), any(Post.class))).thenReturn(1);

                Post updatedPost = new Post();
                updatedPost.setId(100L);
                updatedPost.setTitle("被驳回的帖子");
                updatedPost.setContent("修改后的合规内容");
                when(postMapper.findById(100L)).thenReturn(rejectedPost).thenReturn(updatedPost).thenReturn(updatedPost);

                RiskScoreResult safeResult = RiskScoreResult.safe();
                ModerationDecisionResult approveResult = new ModerationDecisionResult(
                        5, ModerationDecision.AUTO_APPROVE, PostStatus.PUBLISHED.getStatus(), safeResult);
                when(moderationDecisionService.decide(anyString(), eq(currentUser), eq(100L), eq("post")))
                        .thenReturn(approveResult);
                when(postMapper.updateStatus(100L, PostStatus.PUBLISHED.getStatus())).thenReturn(1);
                when(postConverter.toDetailVO(any(Post.class))).thenReturn(postDetailVO);

                PostDetailVO result = postService.updatePost(100L, updateData);

                assertNotNull(result);
                verify(moderationDecisionService).decide(anyString(), eq(currentUser), eq(100L), eq("post"));
                verify(postMapper).updateStatus(100L, PostStatus.PUBLISHED.getStatus());
            }
        }

        @Test
        @DisplayName("被下架的帖子（DELETED 仅管理员操作）编辑被阻止")
        void updatePost_deletedPost_shouldNotAllowEdit() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);

                Post deletedPost = new Post();
                deletedPost.setId(100L);
                deletedPost.setUserId(1L);
                deletedPost.setTitle("已删除帖子");
                deletedPost.setStatus(PostStatus.DELETED.getStatus());

                when(postMapper.findById(100L)).thenReturn(deletedPost);

                Post updateData = new Post();
                updateData.setContent("尝试修改已删除帖子");

                BusinessException ex = assertThrows(BusinessException.class,
                        () -> postService.updatePost(100L, updateData));

                assertEquals(ErrorCode.POST_NOT_FOUND.getCode(), ex.getCode());
                verify(moderationDecisionService, never()).decide(anyString(), any(User.class), anyLong(), anyString());
            }
        }

        @Test
        @DisplayName("SHADOW_BANNED 帖子允许作者编辑并重新审核")
        void updatePost_shadowBannedPost_canBeEditedAndResubmitted() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);

                Post shadowPost = new Post();
                shadowPost.setId(100L);
                shadowPost.setUserId(1L);
                shadowPost.setTitle("影子帖子");
                shadowPost.setContent("原始内容");
                shadowPost.setStatus(PostStatus.SHADOW_BANNED.getStatus());

                when(postMapper.findById(100L)).thenReturn(shadowPost);

                Post updateData = new Post();
                updateData.setContent("修改后的合规内容");
                when(postMapper.updatePost(eq(100L), any(Post.class))).thenReturn(1);

                Post updatedPost = new Post();
                updatedPost.setId(100L);
                updatedPost.setTitle("影子帖子");
                updatedPost.setContent("修改后的合规内容");
                when(postMapper.findById(100L)).thenReturn(shadowPost).thenReturn(updatedPost).thenReturn(updatedPost);

                RiskScoreResult safeResult = RiskScoreResult.safe();
                ModerationDecisionResult approveResult = new ModerationDecisionResult(
                        0, ModerationDecision.AUTO_APPROVE, PostStatus.PUBLISHED.getStatus(), safeResult);
                when(moderationDecisionService.decide(anyString(), eq(currentUser), eq(100L), eq("post")))
                        .thenReturn(approveResult);
                when(postMapper.updateStatus(100L, PostStatus.PUBLISHED.getStatus())).thenReturn(1);
                when(postConverter.toDetailVO(any(Post.class))).thenReturn(postDetailVO);

                PostDetailVO result = postService.updatePost(100L, updateData);

                assertNotNull(result);
                verify(moderationDecisionService).decide(anyString(), eq(currentUser), eq(100L), eq("post"));
            }
        }
    }
}
