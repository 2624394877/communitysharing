package com.taoxin.communitysharing.framework.business.context.config;

import com.taoxin.communitysharing.framework.business.context.interceptor.FeignReqIntereceptor;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class FeignRequestAutoConfiguration {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignReqIntereceptor();
    }
}
