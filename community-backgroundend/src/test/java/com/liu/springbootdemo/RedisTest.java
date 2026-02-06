package com.liu.springbootdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试String类型
     * 场景：验证码、计数器、分布式锁
     */
    @Test
    void testString() {
        // 1.存字符串
        redisTemplate.opsForValue().set("username", "liu_tech");
        System.out.println("String Get: " + redisTemplate.opsForValue().get("username"));

        // 2.存数字（计数器）
        redisTemplate.opsForValue().set("view_count", 100);
        redisTemplate.opsForValue().increment("view_count"); // 自增
        System.out.println("Counter Get: " + redisTemplate.opsForValue().get("view_count"));
        
        // 3.设置过期时间 (10秒后消失)
        redisTemplate.opsForValue().set("code", "123456", 10, TimeUnit.SECONDS);
    }

    /**
     * 测试Hash类型
     * 场景：存储对象（购物车、用户信息）
     */
    @Test
    void testHash() {
        redisTemplate.opsForHash().put("user:1001", "name", "ZhangSan");
        redisTemplate.opsForHash().put("user:1001", "age", 18);
        
        System.out.println("Hash Name: " + redisTemplate.opsForHash().get("user:1001", "name"));
        System.out.println("Hash All: " + redisTemplate.opsForHash().entries("user:1001"));
    }

    /**
     * 测试List类型
     * 场景：消息队列、浏览历史
     */
    @Test
    void testList() {
        // 左进右出 (队列)
        redisTemplate.opsForList().leftPush("history", "post:1");
        redisTemplate.opsForList().leftPush("history", "post:2");
        
        System.out.println("List Pop: " + redisTemplate.opsForList().rightPop("history")); 
    }

    /**
     * 测试Set类型 (无序不重复)
     * 场景：点赞用户、标签集合、共同好友
     */
    @Test
    void testSet() {
        redisTemplate.opsForSet().add("likes:post:1", "user:1", "user:2", "user:1"); // user:1 重复会自动去重
        System.out.println("Set Members: " + redisTemplate.opsForSet().members("likes:post:1"));
        System.out.println("Is Member? " + redisTemplate.opsForSet().isMember("likes:post:1", "user:2"));
    }

    /**
     * 测试ZSet类型 (有序集合)
     * 场景：排行榜 (Rank)
     */
    @Test
    void testZSet() {
        redisTemplate.opsForZSet().add("rank:score", "PlayerA", 100);
        redisTemplate.opsForZSet().add("rank:score", "PlayerB", 80);
        redisTemplate.opsForZSet().add("rank:score", "PlayerC", 90);

        // 取前三名 (倒序)
        System.out.println("Rank Top 3: " + redisTemplate.opsForZSet().reverseRange("rank:score", 0, 2));
    }

    @Test
    void testConnection() {
        // 1. 强制写入一个永不过期的 key
        redisTemplate.opsForValue().set("debug_key", "hello_redis");
        
        // 2. 马上读出来，证明 Java 没问题
        Object value = redisTemplate.opsForValue().get("debug_key");
        System.out.println("Java Read Result: " + value);
        
        // 3. 打印一下当前连的 Redis 配置 (极其重要！)
        // 这一步能看出是不是连到别的地方去了，或者连的是个假的
        System.out.println("Connection Factory: " + redisTemplate.getConnectionFactory());
    }
}