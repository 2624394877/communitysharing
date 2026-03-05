package com.taoxin.communitysharing.user.business.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GlobalCaffeine {
    @Bean
    public Cache<Long, FindUserByIdResDTO> caffeineCache() {
        return Caffeine.newBuilder()
                .initialCapacity(10000)
                .maximumSize(10000)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();
    }
}
