package com.taoxin.communitysharing.oss.business.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage.aliyun-oss")
@Data
public class AliyunProperties {
    private String endpoint;
    private String accessKeyId; // 访问密钥
    private String accessKey; // 密钥
}
