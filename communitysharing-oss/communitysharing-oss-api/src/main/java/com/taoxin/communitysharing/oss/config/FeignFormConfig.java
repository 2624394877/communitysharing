package com.taoxin.communitysharing.oss.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignFormConfig {
    /**
     * 配置feign的form编码器，目的是解决上传文件时，参数无法传递的问题
     * 该编码器将对象转为表单数据，对应的Content-Type为application/x-www-form-urlencoded
     * 或 multipart/form-data
     * @return 返回编码器
     * @Type: Encoder 该类型是Feign的编码器类型，Feign默认的编码器是Encoder
     */
    @Bean
    public Encoder feignFormBeanEncoder() {
        return new SpringFormEncoder();
    }
}
