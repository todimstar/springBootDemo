package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.entity.Comment;
import com.liu.springbootdemo.POJO.entity.Post;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.enums.PostStatus;
import com.liu.springbootdemo.common.enums.UserRole;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.common.utils.SecurityUtil;
import com.liu.springbootdemo.converter.PostConverter;
import com.liu.springbootdemo.mapper.CommentMapper;
import com.liu.springbootdemo.mapper.PostMapper;
import com.liu.springbootdemo.POJO.vo.PostDetailVO;
import com.liu.springbootdemo.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 影子发布可见性控制集成测试
 * US-004: SHADOW_BANNED 帖子/评论的可见性
 * 覆盖场景：发布者视角 vs 其他用户视角 vs 管理员视角
 */
@ExtendWith(MockitoExtension.class)
class ShadowBanVisibilityTest {

    // ======================== Post 相关测试 ========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("帖子详情可见性 - getPostById")
    class PostDetailVisibilityTest {

        @Mock
        private PostMapper postMapper;
        @Mock
        private UserService userService;
        @Mock
        private PostConverter postConverter;
        @InjectMocks
        private PostServiceImpl postService;

        private Post shadowBannedPost;
        private User author;
        private User otherUser;
        private User admin;

        @BeforeEach
        void setUp() {
            shadowBannedPost = new Post();
            shadowBannedPost.setId(100L);
            shadowBannedPost.setUserId(1L);
            shadowBannedPost.setTitle("Shadow Banned Post");
            shadowBannedPost.setStatus(PostStatus.SHADOW_BANNED.getStatus());

            author = new User();
            author.setId(1L);
            author.setUsername("author");
            author.setRole(UserRole.USER.getRoleName());

            otherUser = new User();
            otherUser.setId(2L);
            otherUser.setUsername("other");
            otherUser.setRole(UserRole.USER.getRoleName());

            admin = new User();
            admin.setId(3L);
            admin.setUsername("admin");
            admin.setRole(UserRole.ADMIN.getRoleName());
        }

        @Test
        @DisplayName("发布者本人可以正常查看SHADOW_BANNED帖子")
        void author_canViewShadowBannedPost() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(author);
                when(postMapper.findById(100L)).thenReturn(shadowBannedPost);
                PostDetailVO vo = new PostDetailVO();
                vo.setTitle("Shadow Banned Post");
                when(postConverter.toDetailVO(shadowBannedPost)).thenReturn(vo);

                PostDetailVO result = postService.getPostById(100L);
                assertNotNull(result);
                assertEquals("Shadow Banned Post", result.getTitle());
            }
        }

        @Test
        @DisplayName("其他用户查看SHADOW_BANNED帖子返回404")
        void otherUser_cannotViewShadowBannedPost() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(otherUser);
                when(postMapper.findById(100L)).thenReturn(shadowBannedPost);

                BusinessException ex = assertThrows(BusinessException.class,
                        () -> postService.getPostById(100L));
                assertEquals(ErrorCode.POST_NOT_FOUND.getCode(), ex.getCode());
            }
        }

        @Test
        @DisplayName("未登录用户查看SHADOW_BANNED帖子返回404")
        void anonymousUser_cannotViewShadowBannedPost() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(null);
                when(postMapper.findById(100L)).thenReturn(shadowBannedPost);

                BusinessException ex = assertThrows(BusinessException.class,
                        () -> postService.getPostById(100L));
                assertEquals(ErrorCode.POST_NOT_FOUND.getCode(), ex.getCode());
            }
        }

        @Test
        @DisplayName("管理员可以查看SHADOW_BANNED帖子")
        void admin_canViewShadowBannedPost() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(admin);
                when(postMapper.findById(100L)).thenReturn(shadowBannedPost);
                PostDetailVO vo = new PostDetailVO();
                vo.setTitle("Shadow Banned Post");
                when(postConverter.toDetailVO(shadowBannedPost)).thenReturn(vo);

                PostDetailVO result = postService.getPostById(100L);
                assertNotNull(result);
                assertEquals("Shadow Banned Post", result.getTitle());
            }
        }

        @Test
        @DisplayName("发布者查看自己的帖子无任何影子处理提示")
        void author_noShadowBanHint() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(author);
                when(postMapper.findById(100L)).thenReturn(shadowBannedPost);
                PostDetailVO vo = new PostDetailVO();
                vo.setTitle("Shadow Banned Post");
                when(postConverter.toDetailVO(shadowBannedPost)).thenReturn(vo);

                // 正常返回，不抛异常即为无提示
                PostDetailVO result = postService.getPostById(100L);
                assertNotNull(result);
            }
        }
    }

    // ======================== Comment 相关测试 ========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("评论列表可见性 - findByPostIdByPage")
    class CommentVisibilityTest {

        @Mock
        private CommentMapper commentMapper;
        @Mock
        private PostMapper postMapper;
        @InjectMocks
        private CommentServiceImpl commentService;

        private Post shadowBannedPost;
        private Post publishedPost;
        private User author;
        private User otherUser;
        private User admin;
        private List<Comment> mockComments;

        @BeforeEach
        void setUp() {
            shadowBannedPost = new Post();
            shadowBannedPost.setId(100L);
            shadowBannedPost.setUserId(1L);
            shadowBannedPost.setStatus(PostStatus.SHADOW_BANNED.getStatus());

            publishedPost = new Post();
            publishedPost.setId(200L);
            publishedPost.setUserId(1L);
            publishedPost.setStatus(PostStatus.PUBLISHED.getStatus());

            author = new User();
            author.setId(1L);
            author.setUsername("author");
            author.setRole(UserRole.USER.getRoleName());

            otherUser = new User();
            otherUser.setId(2L);
            otherUser.setUsername("other");
            otherUser.setRole(UserRole.USER.getRoleName());

            admin = new User();
            admin.setId(3L);
            admin.setUsername("admin");
            admin.setRole(UserRole.ADMIN.getRoleName());

            Comment c1 = new Comment();
            c1.setId(1L);
            c1.setContent("test comment");
            mockComments = List.of(c1);
        }

        @Test
        @DisplayName("帖子作者可以查看SHADOW_BANNED帖子的评论")
        void author_canViewCommentsOfShadowBannedPost() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(author);
                when(postMapper.findById(100L)).thenReturn(shadowBannedPost);
                when(commentMapper.findByPostIdByIndex(eq(100L), eq(0), eq(10))).thenReturn(mockComments);

                List<Comment> result = commentService.findByPostIdByPage(100L, 1, 10);
                assertNotNull(result);
                assertEquals(1, result.size());
            }
        }

        @Test
        @DisplayName("其他用户无法查看SHADOW_BANNED帖子的评论")
        void otherUser_cannotViewCommentsOfShadowBannedPost() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(otherUser);
                when(postMapper.findById(100L)).thenReturn(shadowBannedPost);

                BusinessException ex = assertThrows(BusinessException.class,
                        () -> commentService.findByPostIdByPage(100L, 1, 10));
                assertEquals(ErrorCode.POST_NOT_FOUND.getCode(), ex.getCode());
            }
        }

        @Test
        @DisplayName("未登录用户无法查看SHADOW_BANNED帖子的评论")
        void anonymousUser_cannotViewCommentsOfShadowBannedPost() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(null);
                when(postMapper.findById(100L)).thenReturn(shadowBannedPost);

                BusinessException ex = assertThrows(BusinessException.class,
                        () -> commentService.findByPostIdByPage(100L, 1, 10));
                assertEquals(ErrorCode.POST_NOT_FOUND.getCode(), ex.getCode());
            }
        }

        @Test
        @DisplayName("管理员可以查看SHADOW_BANNED帖子的评论")
        void admin_canViewCommentsOfShadowBannedPost() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(admin);
                when(postMapper.findById(100L)).thenReturn(shadowBannedPost);
                when(commentMapper.findByPostIdByIndex(eq(100L), eq(0), eq(10))).thenReturn(mockComments);

                List<Comment> result = commentService.findByPostIdByPage(100L, 1, 10);
                assertNotNull(result);
                assertEquals(1, result.size());
            }
        }

        @Test
        @DisplayName("正常已发布帖子的评论任何人都可查看")
        void anyUser_canViewCommentsOfPublishedPost() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(otherUser);
                when(postMapper.findById(200L)).thenReturn(publishedPost);
                when(commentMapper.findByPostIdByIndex(eq(200L), eq(0), eq(10))).thenReturn(mockComments);

                List<Comment> result = commentService.findByPostIdByPage(200L, 1, 10);
                assertNotNull(result);
                assertEquals(1, result.size());
            }
        }

        @Test
        @DisplayName("未登录用户可查看正常帖子评论")
        void anonymousUser_canViewCommentsOfPublishedPost() {
            try (MockedStatic<SecurityUtil> mockSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                mockSecurity.when(SecurityUtil::getCurrentUser).thenReturn(null);
                when(postMapper.findById(200L)).thenReturn(publishedPost);
                when(commentMapper.findByPostIdByIndex(eq(200L), eq(0), eq(10))).thenReturn(mockComments);

                List<Comment> result = commentService.findByPostIdByPage(200L, 1, 10);
                assertNotNull(result);
                assertEquals(1, result.size());
            }
        }
    }
}
