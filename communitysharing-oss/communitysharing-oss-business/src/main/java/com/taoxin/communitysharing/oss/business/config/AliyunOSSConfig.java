package com.taoxin.communitysharing.oss.business.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliyunOSSConfig {
    @Resource
    private AliyunProperties aliyunProperties;

    @Bean
    public OSS aliyunOSSClient() {
        // 配置访问凭证
//        DefaultCredentialProvider credentialProvider = new DefaultCredentialProvider(aliyunProperties.getAccessKeyId(), aliyunProperties.getAccessKeySecret());
        DefaultCredentialProvider credentialProvider = CredentialsProviderFactory.newDefaultCredentialProvider(aliyunProperties.getAccessKeyId(), aliyunProperties.getAccessKey());
        return new OSSClientBuilder().build(aliyunProperties.getEndpoint(), credentialProvider);
    }
}
