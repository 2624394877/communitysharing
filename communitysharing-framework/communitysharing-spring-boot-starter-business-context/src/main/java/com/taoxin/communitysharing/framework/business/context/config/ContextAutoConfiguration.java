package com.taoxin.communitysharing.framework.business.context.config;


import com.taoxin.communitysharing.framework.business.context.filter.HeaderUserIdContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ContextAutoConfiguration {

    @Bean
    public HeaderUserIdContextFilter loginUserContextHolder() {
        return new HeaderUserIdContextFilter();
    }
}
