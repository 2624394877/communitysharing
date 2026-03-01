package com.taoxin.communitysharing.oss.business.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage.minio")
@Data // 添加getter和setter方法
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
}
