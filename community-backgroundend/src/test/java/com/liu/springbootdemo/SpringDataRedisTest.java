package com.liu.springbootdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;

@SpringBootTest
public class SpringDataRedisTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisTemplate() {
        System.out.println(redisTemplate.toString());
        System.out.println(redisTemplate);

        ValueOperations<String, Object> stringObjectValueOperations = redisTemplate.opsForValue();
        HashOperations<String, Object, Object> stringObjectObjectHashOperations = redisTemplate.opsForHash();
        ListOperations<String, Object> stringObjectListOperations = redisTemplate.opsForList();
        SetOperations<String, Object> stringObjectSetOperations = redisTemplate.opsForSet();
        ZSetOperations<String, Object> stringObjectZSetOperations = redisTemplate.opsForZSet();
    }

    @Test
    public void testStringOperations() {

        redisTemplate.opsForValue().set("java1","javaRedisTest1");
        String value = (String) redisTemplate.opsForValue().get("java1");

        System.out.println("Value for ‘java1’ : " + value);
    }


}
