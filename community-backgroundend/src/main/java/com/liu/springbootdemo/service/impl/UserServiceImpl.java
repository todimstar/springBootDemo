package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.entity.DTO.LoginResponseDTO;
import com.liu.springbootdemo.entity.User;
import com.liu.springbootdemo.exception.InvalidInputException;
import com.liu.springbootdemo.exception.UserAlreadyExistsException;
import com.liu.springbootdemo.exception.UnauthorizedException;
import com.liu.springbootdemo.mapper.UserMapper;
import com.liu.springbootdemo.service.UserService;
import com.liu.springbootdemo.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired  //关键注解：自动注入UserMapper实例，我们可以直接使用了
    private UserMapper userMapper;
    @Autowired  //注入密码加密器
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    // 现在注册时检查邮箱，但是登录时不检查邮箱哦，可能是因为不以邮箱登录吧，邮箱只是作为用户信息吧，之后注册应该也不用邮箱，这个接口是为了用户填写邮箱信息的吧，也可以换成手机号验证
    @Override
    public void register(User user) {

        // 1. 业务逻辑：检查用户名是否已经存在，返回null即为没有该用户，允许注册
        User userByUsername = userMapper.findByUsername(user.getUsername());
        User userByEmail = userMapper.findByEmail(user.getEmail());
        // 已有用户
        if(userByUsername != null){
            // 用户已存在，抛出异常（后续全局异常处理）
            throw new UserAlreadyExistsException("用户名已被占用！");
        }
       if(userByEmail != null){
           throw new UserAlreadyExistsException("该邮箱已被使用！");
       }
       if(user.getPassword().length()<5 || user.getPassword().length()>20){
           throw new InvalidInputException("密码长度必须在5到20个字符之间");
       }

        // 2. 加密
        // user.setPassword(encode(user.getPassword()));
        // 将密码加密并存入user对象
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 3. 调用Mapper层，将数据写入数据库
        userMapper.insert(user);
    }

    @Override
    public LoginResponseDTO login(String usernameOrEmail, String password) {
        // 登录后逻辑
        User userInDbByUsername = userMapper.findByUsername(usernameOrEmail);
        User userInDbByEmail = userMapper.findByEmail(usernameOrEmail);
        // 1. 用户不存在或密码错误
        if((userInDbByUsername == null && userInDbByEmail == null)){
            throw new InvalidInputException("用户名/邮箱未注册，请注册后重试");
        }
        User userInDb = (userInDbByUsername==null?userInDbByEmail:userInDbByUsername);
        if(!passwordEncoder.matches(password,userInDb.getPassword())){
            throw new UnauthorizedException("密码错误！");   //确实就是密码错误
        }//TODO:Redis实现尝试登录次数限制和记录

        // 捕获数据库更新异常
        if (userMapper.updateLogintimeByUsername(userInDb.getUsername()) != 1) {
            // 如果还能走这里，那就是数据库更新失败
            logger.warn("为用户 {} 更新登录时间失败", usernameOrEmail);
            throw new RuntimeException("用户 " + usernameOrEmail+ "更新最新登录时间失败，数据库无报错但返回行数不为1");
        }
        // 构造Spring Security的UserDetails对象
        UserDetails userDetails = loadUserByUsername(userInDb.getUsername());
        // 生成Token：将UserDetails传给JwtUtil实现
        String token = jwtUtil.generateToken(userDetails);

        // 创建返回体

        return new LoginResponseDTO(userInDb.getUsername(),token);
    }

    @Override
    public List<User> getAllUser(){
        return userMapper.getAll();
    }

    @Override   //授权用
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 去数据库查用户在不在
        User myUser = userMapper.findByUsername(username);

        //b. 如果用户不存在，必须抛出此异常，Spring Security会捕获它并认为认证失败
        if(myUser == null){
            throw new UsernameNotFoundException("用户 " + username + " 不存在");
        }

        //用户权限列表，是为了应对单用户多角色的情况。如果真的多角色，数据库要变成user表、role表、user_role中间表这种多对多关系的表结构，然后getRole()要连接表查询返回用户角色列表，本函数中要循环读取用户的多个角色并添加到authorities列表中
        List<GrantedAuthority> authorities = new ArrayList<>();
        //读取用户角色，并转换为SimpleGrantedAuthority对象添加到权限列表中
        authorities.add(new SimpleGrantedAuthority(myUser.getRole()));

        //c. 如果用户存在，将其转换为Spring Security需要的UserDetails对象返回
        return new org.springframework.security.core.userdetails.User(
                myUser.getUsername(),
                myUser.getPassword(),
                authorities     
        );
        
    }
}
