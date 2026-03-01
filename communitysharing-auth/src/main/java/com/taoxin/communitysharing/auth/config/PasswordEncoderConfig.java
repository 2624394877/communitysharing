package com.taoxin.communitysharing.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Configuration
//@Component
@Slf4j
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("=======> 创建密码加密器");
        // 依赖说明： BCryptPasswordEncoder 是 Spring Security 提供的密码加密器，用于对密码进行加密。
        return new BCryptPasswordEncoder();
    }
}
