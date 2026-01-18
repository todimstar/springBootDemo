package com.liu.springbootdemo.config;

import com.liu.springbootdemo.filter.JwtAuthenticationFilter;
import com.liu.springbootdemo.common.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)    // 开启全局方法级@PreAuthorize权限控制
public class SecurityConfig {

    // 注入我们自己创建的JWT过滤器
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        // 开始配置授权请求
        http.authorizeHttpRequests(authorize -> authorize
                // 白名单：对登录和注册路径的请求，允许所有形式访问
                .requestMatchers("/api/auth/register","/api/auth/login").permitAll()
                // Swagger/Knife4j/静态资源 白名单
                .requestMatchers(
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/v3/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/favicon.ico"
                ).permitAll()
                // 白名单：允许Get方法获取帖子列表，无需验证jwt
                .requestMatchers(HttpMethod.GET,
                        "/api/posts",
                        "/api/posts/*", //获取单个帖子，根据帖子id
                        "/api/comments/*/comments",    // 允许Get方法获取单个帖子所有评论
                        "/api/categories",  // 获取分区列表
                        "/api/categories/*", // 获取单个分区信息，根据id
                        "/upload/getUrl"   //获取文件资源url
                ).permitAll()
                                // 默认全拦截
                .anyRequest().authenticated()   // 对于任何其他未匹配的请求，都必须经过身份验证

        )
        // 设置Session管理策略为无状态，给我们的JWT过滤器让道
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // 添加我们的JWT过滤器到过滤器链中
        // 且要在 UsernamePasswordAuthenticationFilter 之前执行
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .csrf(csrf -> csrf.disable()) // 暂时禁用CSRF保护，以便于API测试    //TODO:跟spring实战里，加登录跳转等

        // 已通过JwtFilter，进到spring处理授权。此处的认证失败处理，是通过了jwt过滤器，在上面的授权中报异常的情况
        .exceptionHandling(exceptions -> exceptions
                // 处理未认证的情况
                .authenticationEntryPoint((request, response, authException) -> {
                    ResponseUtil.sendErrorResponse(
                            response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "未登录，请登录后重试"
                            );
                    System.out.println(this.getClass() + "的未认证处理 -> 请求的Method是" + request.getMethod() + " -> RequestURL是" + request.getRequestURI() + " -> 异常信息是" + authException.getMessage());
                })
                .accessDeniedHandler(((request, response, accessDeniedException) -> {
                    ResponseUtil.sendErrorResponse(
                            response,
                            HttpServletResponse.SC_FORBIDDEN,
                            "权限不足，无法访问该资源"
                    );
                    System.out.println(this.getClass() + "的权限不足设置");
                }))
        );

        return http.build();
    }
}