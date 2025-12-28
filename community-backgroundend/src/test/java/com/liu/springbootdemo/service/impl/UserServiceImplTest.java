package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) 是 JUnit 5 的注解，它会启用 Mockito 框架
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    // @Mock: 创建一个 UserMapper 的“模拟对象”。
    // 它是一个假冒的 UserMapper，所有方法的行为都由我们来定义。
    @Mock
    private UserMapper userMapper;

    // @Mock: 同样，创建一个 PasswordEncoder 的模拟对象。
    @Mock
    private PasswordEncoder passwordEncoder;

    // @InjectMocks: 创建一个 UserServiceImpl 的真实实例。
    // 并且，Mockito会自动将上面用 @Mock 创建的模拟对象（userMapper, passwordEncoder）“注入”到这个实例中。
    @InjectMocks
    private UserServiceImpl userService;

    @InjectMocks
    private PostServiceImpl postService;

    // @Test: 这是一个测试方法，测试用户已存在的注册场景是否能正确抛出异常
    @Test
    void register_shouldThrowUserAlreadyExistsException_whenUsernameExists() {
        // --- 1. 准备阶段 (Arrange) ---

        // 创建一个用于测试的User对象
        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setPassword("password123");
        existingUser.setEmail("test@example.com");

        // 定义模拟对象的行为：
        // 当 userMapper 的 findByUsername 方法被以 "testuser" 为参数调用时，
        // 我们“假装”数据库里已经有这个用户了，让它返回我们上面创建的 existingUser 对象。
        when(userMapper.findByUsername("testuser")).thenReturn(existingUser);

        // --- 2. 执行阶段 (Act) & 3. 断言阶段 (Assert) ---

        // 我们断言（assert），当执行 userService.register(existingUser) 这行代码时，
        // 它“必须”抛出 UserAlreadyExistsException.class 这个类型的异常。
        // assertThrows 是 JUnit 5 提供的方法，专门用来测试异常情况。
//        assertThrows(UserAlreadyExistsException.class, () -> {

        //升级为统一异常后，需要先捕获再断言其中ErrorCode属性，如下
        BusinessException exception = assertThrows(BusinessException.class, () -> {   //不知道这样可不可以
            userService.register(existingUser);
        });

        //捕获后验证错误码类型
        assertEquals(ErrorCode.USERNAME_EXISTS.getCode(), exception.getCode());

        // (可选) 验证模拟对象的方法是否从未被调用：
        // 因为用户名已存在，程序应该在加密和插入之前就抛出异常，
        // 所以 passwordEncoder.encode() 和 userMapper.insert() 这两个方法根本不应该被执行。
        // verify(mock, never()) 是一种高级用法，可以确保代码按预期的路径执行。
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).insert(any(User.class));
    }

    // TODO: 你的下一个任务
    // 请参考上面的例子，在这里添加一个新的测试方法，测试“注册成功”的场景。
    // 测试方法名可以叫：register_shouldSaveUser_whenUsernameIsNew()
    // 在这个场景下，你需要:
    // 1. when(userMapper.findByUsername(...)).thenReturn(null); // 假装用户不存在
    // 2. when(passwordEncoder.encode(...)).thenReturn("encodedPassword"); // 假装密码被加密了
    // 3. 调用 userService.register(...)
    // 4. 验证 userMapper.insert() 方法是否被“恰好调用了1次”。(使用 verify(userMapper, times(1)).insert(...); )


    // @Captor: 创建一个参数捕获器，专门用来捕获 User 类型的参数
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Test
    void register_shouldSaveUser_whenUsernameIsNew(){
        // --- Arrange ---
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("12345678");
        // 假装用户不存在
        when(userMapper.findByUsername(testUser.getUsername())).thenReturn(null);
        // 假装邮箱不存在 (你的代码里有邮箱校验，所以测试里也要覆盖)
        when(userMapper.findByEmail(testUser.getEmail())).thenReturn(null);
        // 假装密码加密后会返回一个特定的字符串
        when(passwordEncoder.encode("12345678")).thenReturn("a_very_encoded_password");

        // --- Act ---
        userService.register(testUser);

        // --- Assert ---
        // 1. 验证 insert 方法被调用了，并用捕获器“抓住”传入的User对象
        verify(userMapper).insert(userArgumentCaptor.capture());

        // 2. 从捕获器中获取被“抓住”的User对象
        User capturedUser = userArgumentCaptor.getValue();

        // 3. 对这个被捕获的对象进行精细的断言！
        // 验证存入数据库的用户名是不是我们期望的
        assertEquals("testUser", capturedUser.getUsername());
        // 验证存入数据库的密码是不是【加密后】的密码，而不是原始密码！
        assertEquals("a_very_encoded_password", capturedUser.getPassword());
    }


}
