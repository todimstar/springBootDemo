package com.liu.springbootdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication
@ComponentScan(
        basePackages = "com.liu.springbootdemo",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com.liu.springbootdemo.service.impl.ais.*"
        )
)
@EnableTransactionManagement    //开启注解实现事务管理
@MapperScan("com.liu.springbootdemo.mapper")
public class SpringBootDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootDemoApplication.class, args);
    }

}
