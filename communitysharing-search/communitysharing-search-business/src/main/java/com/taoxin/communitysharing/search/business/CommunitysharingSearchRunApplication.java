package com.taoxin.communitysharing.search.business;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.taoxin.communitysharing.search.business.domain.mapper")
public class CommunitysharingSearchRunApplication {
    public static void main( String[] args ) {
        SpringApplication.run(CommunitysharingSearchRunApplication.class, args);
    }
}
