package com.taoxin.communitysharing.content.business;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.taoxin.communitysharing.content.business.domain.mapper")
@EnableFeignClients(basePackages = {"com.taoxin.communitysharing"})
public class CommunitysharingContentApplication
{
    public static void main(String[] args) {
        SpringApplication.run(CommunitysharingContentApplication.class, args);
    }
}
