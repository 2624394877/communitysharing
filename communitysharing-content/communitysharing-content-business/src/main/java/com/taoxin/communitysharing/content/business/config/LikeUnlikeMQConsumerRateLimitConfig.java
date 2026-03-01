package com.taoxin.communitysharing.content.business.config;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class LikeUnlikeMQConsumerRateLimitConfig {
    @Value("${mq-consumer.like-Unlike.rate-limit}")
    private double limit;

    @Bean
    @RefreshScope
    public RateLimiter rateLimiter() {
        return RateLimiter.create(limit); // 令牌桶限流
    }
}
