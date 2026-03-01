package com.taoxin.communitysharing.user.business;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.taoxin.communitysharing.user.business.domain.mapper")
@EnableFeignClients(basePackages = {"com.taoxin.communitysharing"})
public class CommunitysharingUserBusinessApplication
{
    public static void main( String[] args ) {
        SpringApplication.run(CommunitysharingUserBusinessApplication.class, args);
    }
}
