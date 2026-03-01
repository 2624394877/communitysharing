package com.taoxin.communitysharing.auth.smtp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.mail")
@Component
@Data
public class EmailProperties {
    private String host;
    private String username;
    private String password;
}
