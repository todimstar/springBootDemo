package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.entity.Post;
import com.liu.springbootdemo.entity.User;
import com.liu.springbootdemo.mapper.PostMapper;
import com.liu.springbootdemo.mapper.UserMapper;
import com.liu.springbootdemo.utils.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private UserMapper userMapper;

//    // @Mock: 我们需要模拟 SecurityContext 和 Authentication
//    @Mock
//    private SecurityContext securityContext;
//
//    @Mock
//    private Authentication authentication;

    @InjectMocks
    private PostServiceImpl postService;

    @Captor
    private ArgumentCaptor<Post> postArgumentCaptor;

    // @BeforeEach: 在执行每一个 @Test 方法之前，都会先执行一次这个方法。
    // 我们用它来统一设置模拟登录状态，避免在每个测试里重复写。
    @BeforeEach
    void setUp() {
        //都不需要了，因为从静态方法获取用户
//        // 步骤1: 将模拟的 authentication 对象设置到模拟的 securityContext 中
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        // 步骤2: 将这个模拟的 securityContext 设置到 SecurityContextHolder 中
//        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createPost_shouldSetUserIdAndSavePost() {
        // --- 1. 准备阶段 (Arrange) ---

        // a. 准备要创建的帖子数据 (来自客户端)
        Post postFromClient = new Post();
        postFromClient.setTitle("Test Title");
        postFromClient.setContent("Test Content");

        // b. 准备模拟的UserDetails和User对象，代表当前登录的用户
        User currentUser = new User();
        currentUser.setId(123L); // 设定一个明确的用户ID
        currentUser.setUsername("testuser");


        // c. 定义关键的模拟行为
        MockedStatic<SecurityUtil> mockSecurityUtil = Mockito.mockStatic(SecurityUtil.class);

        //在任何代码调用SecurityUtil.getCurrentUser()时，返回我们Mock的currentUser
        mockSecurityUtil.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
        //在任何代码调用postMapper.findById()时，返回我们Mock的postFromClient
        when(postMapper.findById(any())).thenReturn(postFromClient);

        /*更改了用户获取方法，所以改成以上模拟静态SecurityUtil方法*/
//        UserDetails userDetails = mock(UserDetails.class); //直接用mock()方法创建一个UserDetails的模拟对象
//        // 当代码尝试获取当前认证主体时，返回我们模拟的 userDetails
//        when(authentication.getPrincipal()).thenReturn(userDetails);
//        // 当代码尝试获取用户名时，返回 "testuser"
//        when(userDetails.getUsername()).thenReturn("testuser");
//        // 当代码用 "testuser" 去数据库查User对象时，返回我们准备好的 currentUser
//        when(userMapper.findByUsername("testuser")).thenReturn(currentUser);

        // --- 2. 执行阶段 (Act) ---
        postService.createPost(postFromClient);

        // --- 3. 断言阶段 (Assert) ---

        // a. 捕获传入 postMapper.insert() 的Post对象
        verify(postMapper).insert(postArgumentCaptor.capture());
        Post savedPost = postArgumentCaptor.getValue();

        // b. 验证被保存的Post对象的userId是否被正确设置成了当前登录用户的ID
        assertEquals(123L, savedPost.getUserId());
        // c. (可选) 验证标题和内容也正确传递了
        assertEquals("Test Title", savedPost.getTitle());
    }
}