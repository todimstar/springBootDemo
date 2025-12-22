package com.liu.springbootdemo.config;

import com.liu.springbootdemo.common.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 配置和拓展MVC功能
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer{

    /**
     * 跨域资源共享（CORS）配置
     * 允许来自指定源的跨域请求
     * 后端层面的
     */
    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")// 1.对所有API路径生效    
                .allowedOrigins("http://localhost:5173") //2.允许这个源的请求
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")// 3. 允许的HTTP方法
                .allowCredentials(true)// 4.允许携带Cookie等凭证
                .maxAge(3600); // 5.预检请求的有效期，单位秒 --> 预检就是网页跨域前会发来的OPTIONS请求"投石问路"，这里有效期就是允许授权后3600s不用再预检
    }

//    /**
//     * 扩展MVC的消息转换器，注册上自己的时间转换器
//     * @param converters
//     */
//    public void extendMessageConverters(List<HttpMessageConverter<?>> converters){
//        log.info("扩展消息转换器...");
//
//        //创建一个消息转换器对象占位
//        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
//        //设置对象转换器，传入可以将Java对象转换为JSON字符串的对象转换器类
//        converter.setObjectMapper(new JacksonObjectMapper());
//
//        //将上面的消息转换器对象追加到MVC框架的转换器集合中
//        converters.add(0,converter);
//    }

}