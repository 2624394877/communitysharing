package com.taoxin.communitysharing.algin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.taoxin.communitysharing.algin.domain.mapper")
@EnableFeignClients(basePackages = "com.taoxin.communitysharing")
public class CommunitysharingAlginRunApplication {
    public static void main( String[] args ) {
        SpringApplication.run(CommunitysharingAlginRunApplication.class, args);
    }
}
