package com.taoxin.communitysharing.count.business.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisTemplateConfig {

    /**
     * 配置RedisTemplate，设置序列化方式
     * @param redisConnectionFactory Redis连接工厂
     * @return RedisTemplate 返回将键值对存储在Redis中的模板（String&Object）
     */
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // key序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // hash key序列化（非常重要）
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // value序列化
        Jackson2JsonRedisSerializer<Object> jsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(Object.class);

        redisTemplate.setValueSerializer(jsonRedisSerializer);

        // hash value序列化
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
