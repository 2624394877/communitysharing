package com.taoxin.communitysharing.user.relation.buiness;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.taoxin.communitysharing.user.relation.buiness.domain.mapper")
@EnableFeignClients(basePackages = {"com.taoxin.communitysharing"})
public class CommunitysharingUserRelationBusinessApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunitysharingUserRelationBusinessApplication.class, args);
    }
}
