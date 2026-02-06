package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.POJO.dto.user.LoginInControllerDTO;
import com.liu.springbootdemo.POJO.dto.user.RegisterDTO;
import com.liu.springbootdemo.POJO.dto.user.UpdateUserDTO;
import com.liu.springbootdemo.POJO.vo.LoginResponseVO;
import com.liu.springbootdemo.POJO.Result.Result;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.service.MinioService;
import com.liu.springbootdemo.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController // 关键注解：表明这是一个控制器，且所有方法返回的都是JSON数据
@RequestMapping("api/auth") //定义这个控制器下所有接口的统一前缀
@Validated
@Slf4j
public class UserController {
    @Autowired  //自动注入UserService实例
    private UserService userService;

    @Autowired  // 用于上传用户头像
    private MinioService minioService;

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9]+([_\\-\\.][A-Za-z0-9]+)*@[A-Za-z0-9]+([\\-\\.][A-Za-z0-9]+)*\\.[A-Za-z]{2,}$";


    @PostMapping("/register")   //定义处理POST请求的接口，路径为 /api/user/register
    @SecurityRequirements() // 标记此接口不需要鉴权
    public Result register(@RequestBody @Valid RegisterDTO registerDTO){ //  @RequestBoby：将请求体重JSON数据自动转换成User对象

        // decode password  解密还原密码

        userService.register(registerDTO); //2.调用业务层注册方法

        return Result.success();    //升级使用ResponseEntity返回自定义201响应头
    }

    @PostMapping("/login")   //登录路径为 /api/user/login
    @SecurityRequirements() // 标记此接口不需要鉴权
    public ResponseEntity<Result<LoginResponseVO>> login(@RequestBody LoginInControllerDTO loginInControllerDTO){
        log.info(String.valueOf(loginInControllerDTO));
        if(!StringUtils.hasText(loginInControllerDTO.getUsernameOrEmail())){
            throw new BusinessException(ErrorCode.EMPTY_USERNAME_OR_EMAIL);
        }
        if(!StringUtils.hasText(loginInControllerDTO.getPassword())){
            throw new BusinessException(ErrorCode.EMPTY_PASSWORD);
        }
        //不需要验证邮箱格式了，因为是用户名或邮箱，直接去查就好了
        // 认证
        LoginResponseVO loginResponseVO = userService.login(loginInControllerDTO.getUsernameOrEmail(), loginInControllerDTO.getPassword());    //账密错误走全局异常

        return ResponseEntity.status(HttpStatus.OK).body(Result.success(loginResponseVO));
    }

    /**
     * 头像上传接口，用于用户已注册更新时的头像上传
     * 要鉴权，不让游客来爆破
     * @param file 头像
     * @return url 头像可访问url: 服务器ip+唯一标识名
     */
    @PostMapping("/upload/userAvatar")
    public Result<String> uploadUserAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        String url = userService.uploadUserAvatar(file);

        return Result.success(url);
    }

    /**
     * 发送注册验证码到邮箱,选参为"注册"
     * @param email
     * @return
     */
    @PostMapping("/sendRegisterCode")
    public Result sendRegisterCode(@RequestParam @Email String email){
        userService.sendRegisterCode(email);
        return Result.success();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<User>> getAllUser(){
        return Result.success(userService.getAllUser());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result deleteHeadByIdForAdmin(@PathVariable
                                                   @Min(value = 0,message = "用户id下限0")
                                                   @Max(value = Long.MAX_VALUE,message = "都破Long了这UserId")
                                                   Long id){
        userService.deleteHeadByIdForAdmin(id);
        return Result.success("删除用户成功");
    }

}
