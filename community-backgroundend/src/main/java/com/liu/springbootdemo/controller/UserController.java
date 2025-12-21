package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.POJO.dto.LoginInControllerDTO;
import com.liu.springbootdemo.POJO.dto.LoginResponseDTO;
import com.liu.springbootdemo.POJO.vo.Result.Result;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.exception.InvalidInputException;
import com.liu.springbootdemo.service.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // 关键注解：表明这是一个控制器，且所有方法返回的都是JSON数据
@RequestMapping("api/auth") //定义这个控制器下所有接口的统一前缀
@Validated
public class UserController {
    @Autowired  //自动注入UserService实例
    private UserService userService;

    @Autowired  // 注入UserDetailsService，用于加载用户详情
    private UserDetailsService userDetailsService;

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9]+([_\\-\\.][A-Za-z0-9]+)*@[A-Za-z0-9]+([\\-\\.][A-Za-z0-9]+)*\\.[A-Za-z]{2,}$";

    // DTO - username -password

    @PostMapping("/register")   //定义处理POST请求的接口，路径为 /api/user/register
    public ResponseEntity<Result> register(@RequestBody User user){ //  @RequestBoby：将请求体重JSON数据自动转换成User对象

        // decode password  解密还原密码

        //0.空检查，检查一下邮箱等传来没有，不然以后不好登录 -->虽然一个是前端保证的，但是不信任先
        if(!StringUtils.hasText(user.getUsername())){
            throw new InvalidInputException("请输入用户名");
        }
        if(!StringUtils.hasText(user.getEmail())){
            throw new InvalidInputException("请输入邮箱?");
        }
        if(!StringUtils.hasText(user.getPassword())){
            throw new InvalidInputException("请输入密码");
        }
        if (!(user.getEmail().matches(EMAIL_REGEX))) {
            throw new InvalidInputException("邮箱格式不正确");
        }

        System.out.println(user);
        userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success());    //升级使用ResponseEntity返回自定义201响应头
        //TODO:还可以转.created(local_url).bady(Result.succes())，这种提供新url的地方，之后再说吧，不会url构建
    }

    @PostMapping("/login")   //登录路径为 /api/user/login
    public ResponseEntity<Result<LoginResponseDTO>> login(@RequestBody LoginInControllerDTO loginInControllerDTO){
        System.out.println(loginInControllerDTO);
        if(!StringUtils.hasText(loginInControllerDTO.getUsernameOrEmail())){
            throw new InvalidInputException("请输入用户名/邮箱");
        }
        if(!StringUtils.hasText(loginInControllerDTO.getPassword())){
            throw new InvalidInputException("请输入密码");
        }
        //不需要验证邮箱格式了，因为是用户名或邮箱，直接去查就好了
        // 认证
        LoginResponseDTO loginResponseDTO = userService.login(loginInControllerDTO.getUsernameOrEmail(), loginInControllerDTO.getPassword());    //账密错误走全局异常

        return ResponseEntity.status(HttpStatus.OK).body(Result.success(loginResponseDTO));
    }

    // @PostMapping("/login")   //登录路径为 /api/user/login
    // public String login(@RequestBody User loginUser){
        
    //     try{
    //         User user = userService.login(loginUser.getUsername(),loginUser.getPassword());    //账密错误走catch
    //         //其实除非多线程，不然这里包返回不是null的，null会被userService里就报错退出
            
    //         if(user != null){   
    //             System.out.println("有账号"+ user +",登录成功");
    //             // return "用户:"+ user.getUsername() +"登录成功";  //返回简单的成功欢迎信息
    //             return JwtUtil.generateToken(user.getUsername()); //返回token
    //         }
    //         //不在登录端口返回用户名错误信息，可以降低被暴力获取网站用户名列表风险
    //         // 可以在注册端口返回，因为注册端口可以实施验证码卡恶意脚本
    //         // 且注册只用一次，登录是很频繁的，所以可以卡注册，不好卡登录，提升用户体感
            
    //         //else{  //活到这里的是null，没有账号，除非多线程才能导致未预料的错误
    //         //     return "账号未注册，请注册后再试";  //劝退信息
    //         // }
    //         return "未预料的错误";
            
    //     }catch(RuntimeException e){
    //         System.out.println("登录失败，未有账号？"+e);
    //         return e.getMessage();  //返回userService报上来的账密错误信息
    //     }
    // }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<User>> getAllUser(){
        return Result.success(userService.getAllUser());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteHeadByIdForAdmin(@PathVariable
                                                   @Min(value = 0,message = "用户id下限0")
                                                   @Max(value = Long.MAX_VALUE,message = "都破Long了这UserId")
                                                   Long id){
        userService.deleteHeadByIdForAdmin(id);
        return Result.success("删除用户成功");
    }

}
