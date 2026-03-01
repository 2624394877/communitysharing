package com.taoxin.communitysharing.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.taoxin.communitysharing", "com.communitysharing"})
public class CommunitysharingGatewayApplication
{
    public static void main( String[] args ){
        SpringApplication.run(CommunitysharingGatewayApplication.class, args);
    }
}
