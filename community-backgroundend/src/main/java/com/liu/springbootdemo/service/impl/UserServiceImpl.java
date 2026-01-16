package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.dto.user.RegisterDTO;
import com.liu.springbootdemo.POJO.dto.user.UpdateUserDTO;
import com.liu.springbootdemo.POJO.vo.LoginResponseVO;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.vo.UpdateUserVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.enums.VERCODE;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.converter.UserConverter;
import com.liu.springbootdemo.mapper.UserMapper;
import com.liu.springbootdemo.service.EmailService;
import com.liu.springbootdemo.service.UserService;
import com.liu.springbootdemo.utils.JwtUtil;
import com.liu.springbootdemo.utils.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired  //关键注解：自动注入UserMapper实例，我们可以直接使用了
    private UserMapper userMapper;
    @Autowired  //注入密码加密器
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserConverter userConverter;

    // 现在注册时检查邮邮箱登录吧，邮箱只是作为用户信息吧，之后注册应该也不用邮箱，这个接口是为了用户填写邮箱信息的吧，也可以换成手机号验证
    @Override
    public void register(RegisterDTO registerDTO) {

        // 1. 业务逻辑：检查用户名是否已经存在，返回null即为没有该用户，允许注册
        User userByUsername = userMapper.findByUsername(registerDTO.getUsername());
        User userByEmail = userMapper.findByEmail(registerDTO.getEmail());
        // 已有用户
        if(userByUsername != null){
            // 用户已存在，抛出异常（后续全局异常处理）
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if(userByEmail != null){
           throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
        if(registerDTO.getPassword().length()<6 ){
           throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        //2. 验证码是否在redis中存在且正确，验证通过后删除验证码,GOOD:使用Redis存储验证码，避免了数据库的读写压力，同时设置过期时间提高安全性
        String redisKey = VERCODE.REGISTER.getRedisKey() + registerDTO.getEmail();
        Object redisCode = redisTemplate.opsForValue().get(redisKey);
        if(redisCode == null){// 验证码过期
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }else if(!redisCode.equals(registerDTO.getVerCode())){// 验证码错误
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR);
        }else{// 验证码正确，删除验证码
            redisTemplate.delete(redisKey);
        }

        // 2. 加密
        // user.setPassword(encode(user.getPassword()));
        // 将密码加密并存入user对象
        registerDTO.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        // 3. 调用Mapper层，将数据写入数据库
        userMapper.insert(userConverter.registerDtoToUser(registerDTO));
    }

    @Override
    public LoginResponseVO login(String usernameOrEmail, String password) {
        // 登录后逻辑
        User userInDbByUsername = userMapper.findByUsername(usernameOrEmail);
        User userInDbByEmail = userMapper.findByEmail(usernameOrEmail);
        // 1. 用户不存在或密码错误
        if((userInDbByUsername == null && userInDbByEmail == null)){
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND,"用户名/邮箱未注册，请注册后重试");
        }
        // 这里已经保证了userInDbByUsername和userInDbByEmail至少有一个不为null
        User userInDb = (userInDbByUsername==null?userInDbByEmail:userInDbByUsername);
        
        //user存在，如果被封禁则遣返
        if(userInDb.isBanned()){
            throw new BusinessException(ErrorCode.USER_BANNED,String.format("用户已被封禁,因%s",userInDb.getBanReason())); //FIXME:加一个用户登录时校验是否被封禁的逻辑，并人性化返回被封禁原因
        }

        //GOOD:Redis实现尝试登录次数限制和记录
        String failKey = "login:fail:"+userInDb.getId().toString();
        //先查是否已经锁定
        Integer failCount = (Integer) redisTemplate.opsForValue().get(failKey);
        if(failCount != null && failCount >= 5){
            long expire = redisTemplate.getExpire(failKey, TimeUnit.MINUTES);
            throw new BusinessException(ErrorCode.FAILED_LOGIN_ATTEMPTS_EXCEEDED,"账号锁定，请等待"+(expire+1)+"分钟");
        }
        //再验证密码正确性
        if(!passwordEncoder.matches(password,userInDb.getPassword())){
            long count = redisTemplate.opsForValue().increment(failKey);
            if(count == 1){
                redisTemplate.expire(failKey, 15, TimeUnit.MINUTES);//首次输错才开始计时15分钟，防止隔天多记
            }
            throw new BusinessException(ErrorCode.WRONG_PASSWORD, "密码错误，还剩 " + (5 - count)+ " 次机会");
        }


        // 更新登录数据，同时捕获数据库更新异常
        if (userMapper.updateLogintimeByUsername(userInDb.getUsername()) != 1) {
            // 如果还能走这里，那就是数据库更新失败
            logger.warn("为用户 {} 更新登录时间失败", usernameOrEmail);
            throw new BusinessException(ErrorCode.USER_UPDATE_FAILED,"用户 " + usernameOrEmail+ "更新最新登录时间失败，数据库无报错但返回行数不为1");
        }
        // 构造Spring Security的UserDetails对象
        UserDetails userDetails = loadUserByUsername(userInDb.getUsername());
        // 生成Token：将UserDetails传给JwtUtil实现
        String token = jwtUtil.generateToken(userDetails);

        //登录成功洗白Redis记录
        redisTemplate.delete(failKey);

        // 创建返回体
        return new LoginResponseVO(userInDb.getUsername(),token);
    }

    /**
     * 发送注册验证码到邮箱,选参为"注册"
     * 校验邮箱是否已被注册
     * 调用通用发送验证码接口
     * @param email
     */
    @Override
    public void sendRegisterCode(String email) {
        User userByEmail = userMapper.findByEmail(email);
        if(userByEmail != null){
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
        sendVerificationCode(email, VERCODE.REGISTER.getCodeType());
    }

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9]+([_\\-\\.][A-Za-z0-9]+)*@[A-Za-z0-9]+([\\-\\.][A-Za-z0-9]+)*\\.[A-Za-z]{2,}$";
    /**
     * 发送邮箱验证码，通用带参版
     * @param email
     * @param mailType 可选验证码信息标注，可为空
     */
    @Override
    public void sendVerificationCode(String email, String mailType) {
        //检查redis中是否存在未过期的验证码
        String redisKey = VERCODE.REGISTER.getRedisKey() + email;
        if(redisTemplate.opsForValue().get(redisKey) != null){
            //获取验证码剩余过期时间
            Long expire = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            logger.warn("邮箱 {} 的验证码未过期，剩余 {} 秒", email, expire);
            throw new BusinessException(ErrorCode.INPUT_INVALID, "验证码已发送，请稍后再试");
        }
        // 校验邮箱格式->也可以限制邮箱类型，禁掉临时邮箱等
        if(!email.matches(EMAIL_REGEX)){
            throw new BusinessException(ErrorCode.EMAIL_INVALID);
        }
        // 生成验证码并发送
        String code = emailService.generateVerificationCode();
        emailService.sendCode(email,code,mailType);
        // 将验证码存入Redis，设置10分钟过期时间
        redisTemplate.opsForValue().set(redisKey, code, VERCODE.REGISTER.getTimeoutMinutes(), TimeUnit.MINUTES);
    }

    /**
     * 获取所有用户，给管理员接口调用,之后可能分页
     * @return List<User>
     */
    @Override
    public List<User> getAllUser(){
        return userMapper.getAll();
    }

    /**
     * 给Service层其他类调用的，根据id获取用户，找不到抛异常
     * @param id
     * @return User
     */
    @Override
    public User getUserById(Long id){
        User user = userMapper.findById(id);
        if(user == null){
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 给接口和其他Service用的，自获取当前登录的用户id去更新数据库
     * @param updateUserDTO
     * @return 用户级别的VO
     */
    @Override
    public UpdateUserVO updateUser(UpdateUserDTO updateUserDTO) {
        //获取当前用户
        User currentUser = SecurityUtil.getCurrentUser();
        if(currentUser == null){//未登录或登录已过期
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"Unbelievable! 你是怎么进来的，谁让你没登录就进来的!💢 滚出去😡*");
        }
        //内容校验？目前都在DTO@Vailded完了
        User user = userConverter.UpdateDtoTOUser(updateUserDTO);
        user.setId(currentUser.getId());
        //更新去Mapper
        userMapper.updateUser(user);
        return null;
    }

    @Override
    public void deleteHeadByIdForAdmin(Long id) throws UsernameNotFoundException{
        // 验用户存在性
        User existUser = userMapper.findById(id);

        if(existUser == null){
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);  //NOTE: 异常升级途中升级了此处原本抛出的专类异常
        }

        // 删除并检查返回值
        if(userMapper.deleteById(id)!=1){
            throw new BusinessException(ErrorCode.USER_DELETE_FAILED);
        }
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
