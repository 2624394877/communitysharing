package com.taoxin.communitysharing.user.business.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RocketMQAutoConfiguration.class) // 导入RocketMQ的配置类
public class RocketMQConfig {
}
