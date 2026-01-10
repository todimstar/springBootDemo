package com.liu.springbootdemo.service;

import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.enums.VERCODE;
import com.liu.springbootdemo.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 生成六位数字验证码
     * @return 验证码字符串
     */
    public String generateVerificationCode() {
        int code = new Random().nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    /**
     * 异步发送验证码邮件
     * @param toEmail 收件人邮箱
     * @param code 验证码
     * @param mailType 验证码种类说明
     */
    @Async
    public void sendCode(String toEmail, String code, String mailType){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            // 设置发件人显示名称，格式为："昵称 <邮箱地址>"
            // 注意：邮箱地址必须是 spring.mail.username 配置的账号，否则会被 139 服务器拒绝
            String from = "TechForum <" + fromEmail + ">"; 
            message.setFrom(from);
            
            message.setTo(toEmail); //收件人
            if(VERCODE.REGISTER.getCodeType().equals(mailType)) {
                message.setSubject("【TechForum】您的注册验证码");  //标题
                message.setText("欢迎注册 TechForum 社区！\n\n您的验证码是：" + code + "\n\n有效期 " + VERCODE.REGISTER.getTimeoutMinutes() +" 分钟，请勿泄露给他人。\n\n若非本人操作，请忽略");//正文
            }else{
                message.setSubject("【TechForum】验证码");  //标题
                message.setText("您的验证码是：" + code + "\n\n有效期 5 分钟。若非本人操作，请忽泄露，谨防被骗");//正文
            }

            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人：{}", toEmail);
        }catch (Exception e){
            log.error("验证码邮件发送失败，收件人：{}，错误信息：{}", toEmail, e.getMessage());
        }
    }

}
