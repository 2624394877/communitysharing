package com.taoxin.communitysharing.auth;

//import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//@MapperScan("com.taoxin.communitysharing.auth.domain.mapper") // 扫描mapper
@EnableFeignClients({"com.taoxin.communitysharing"}) // 扫描FeignClient
public class CommunitysharingAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunitysharingAuthApplication.class, args);
    }

}
