package com.liu.springbootdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")// 1.对所有API路径生效    
                .allowedOrigins("http://localhost:5173") //2.允许这个源的请求
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")// 3. 允许的HTTP方法
                .allowCredentials(true)// 4.允许携带Cookie等凭证
                .maxAge(3600); // 5.预检请求的有效期，单位秒 --> 预检就是网页跨域前会发来的OPTIONS请求"投石问路"，这里有效期就是允许授权后3600s不用再预检
    }
}