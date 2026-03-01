package com.taoxin.communitysharing.count.business;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.taoxin.communitysharing.count.business.domain.mapper")
public class CommunitysharingCountApplication
{
    public static void main( String[] args ) {
        SpringApplication.run(CommunitysharingCountApplication.class, args);
    }
}
