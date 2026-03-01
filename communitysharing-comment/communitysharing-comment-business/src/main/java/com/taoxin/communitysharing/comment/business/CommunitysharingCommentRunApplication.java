package com.taoxin.communitysharing.comment.business;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@MapperScan("com.taoxin.communitysharing.comment.business.domain.mapper")
@EnableRetry
@EnableFeignClients(basePackages = "com.taoxin.communitysharing")
public class CommunitysharingCommentRunApplication {
    public static void main( String[] args ) {
        SpringApplication.run(CommunitysharingCommentRunApplication.class, args);
    }
}
