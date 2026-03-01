package com.taoxin.communitysharing.auth;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
@Slf4j
public class RedisTest {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedis() {
        // 存储数据
        redisTemplate.opsForValue().set("name", "Taoxin");
        // 获取数据
        Object name = redisTemplate.opsForValue().get("name");
        log.info("name: {}", name);
    }
}
