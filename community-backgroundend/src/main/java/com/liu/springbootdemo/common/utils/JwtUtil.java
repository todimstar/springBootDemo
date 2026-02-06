package com.liu.springbootdemo.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@ConfigurationProperties(prefix = "community.jwt")    //配置在properties或yml文件的sky.jwt结构下
@Data
public class JwtUtil {

    private String SECRET_KEY;

    // jwtToken过期时间
    private long EXPIRATION_TIME; //24小时*7天毫秒

    // 1. 验证Token是否有效
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // 检查用户名是否匹配，并且Token没有过期
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // ⬇️检查Token是否已过期
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ⬇️从Token中提取用户名
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    // ⬇️从Token中提取过期时间
    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    // ⬇️核心提取逻辑：从Token中提取指定的Claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 解析Token，获取所有的Claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // 使用密钥验证签名
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 2. 生成Token的核心方法
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // 您可以在这里添加更多的自定义信息到Token中，比如用户的角色
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    // ⬇️创建Token的具体实现
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // 设置自定义的Claims
                .subject(subject) // 设置主题，通常是用户名
                .issuedAt(new Date(System.currentTimeMillis())) // 设置签发时间
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
                .signWith(getSigningKey()) // 使用指定的密钥和算法进行签名
                .compact();
    }

    // ⬇️获取用于签名的密钥对象
    private SecretKey getSigningKey() {
        // 将我们定义的字符串密钥，转换为加密算法需要的SecretKey对象
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }


}
