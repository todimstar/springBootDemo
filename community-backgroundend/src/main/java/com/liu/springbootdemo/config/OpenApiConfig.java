package com.liu.springbootdemo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                //定义文档封面信息
                .info(new Info()
                        .title("Community API")
                        .description("社区后端接口文档")
                        .version("v1.1.0")
                        )

                // 定义BearerAuth组件，用于加入全局鉴权列表
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",   //定义一个名为BearerAuth的鉴权组件
                                new SecurityScheme()
                                        .name("BearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // 配置全局鉴权参数列表，加入BearerAuth
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }

}

