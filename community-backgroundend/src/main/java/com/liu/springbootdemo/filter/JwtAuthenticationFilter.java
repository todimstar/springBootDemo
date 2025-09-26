package com.liu.springbootdemo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.springbootdemo.entity.VO.Result;
import com.liu.springbootdemo.utils.JwtUtil;
import com.liu.springbootdemo.utils.ResponseUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component  //将该过滤器注册为Spring容器中的一个Bean
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头中获取 "Authorization" 字段
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. 检查请求头是否有效
        // 如果请求头为空，或者不以 "Bearer " 开头，则直接放行，让后续的过滤器去处理
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {// Bearer JWT无状态认证的标识,还有两种有状态认证标识 [Basic Base64编码的用户名密码，Digest MD5码摘要认证]
            filterChain.doFilter(request, response); // 放行
            return;
        }

        // 3. 提取JWT Token (去掉 "Bearer " 前缀)
        jwt = authHeader.substring(7);

        try {
            // 4. 从Token中解析出用户名
            username = jwtUtil.extractUsername(jwt);

            // 5. 核心验证逻辑
            // 检查用户名不为空，并且【当前安全上下文中没有已认证的用户信息】
            // SecurityContextHolder.getContext().getAuthentication() == null 是为了防止重复认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // a. 根据用户名加载用户的详细信息 (UserDetails)
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // b. 验证Token是否有效（用户名匹配且未过期）
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // c. 如果Token有效，则构建一个【已认证】的 Authentication 对象
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // 我们已经验证过Token了，所以不需要凭证(credentials)
                            userDetails.getAuthorities() // 用户的权限信息
                    );
                    // 可选: 保存请求的ip、session等细节信息以便日志审查
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // d. 【最关键的一步】将这个已认证的对象，设置到安全上下文中
                    // 这样，Spring Security就知道当前请求的用户是谁，以及他拥有什么权限，其他模块也可以通过SecurityContextHolder获取当前登录用户信息
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // 6. 无论验证成功与否，都放行请求，让它继续走向下一个过滤器或目标接口
            filterChain.doFilter(request, response);
        }catch (ExpiredJwtException e){
            // jwt过期，写回响应返回401,UnAuthorized
            ResponseUtil.sendErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "登录过期，请重新登陆");
            System.out.println(this.getClass() + "的jwt登录过期");
            //无过滤器链后续执行
        }

    }
}
