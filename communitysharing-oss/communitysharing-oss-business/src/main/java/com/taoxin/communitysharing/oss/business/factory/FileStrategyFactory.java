package com.taoxin.communitysharing.oss.business.factory;

import com.taoxin.communitysharing.oss.business.strategy.impl.AliyunFileStrategy;
import com.taoxin.communitysharing.oss.business.strategy.impl.MinioFileStrategy;
import com.taoxin.communitysharing.oss.business.strategy.inface.FileStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件策略工厂
 */
@Configuration
@RefreshScope // 配置文件热更新，当配置中心的配置发生变化时，应用中的配置会自动更新
public class FileStrategyFactory {
    @Value("${storage.type}")
    private String storageType;

    @Bean
    @RefreshScope
    public FileStrategy setFileStrategy() {

        if (StringUtils.equals("minio", storageType)) {
            return new MinioFileStrategy();
        } else if (StringUtils.equals("aliyun", storageType)) {
            return new AliyunFileStrategy();
        }else {
            throw new IllegalArgumentException("不支持的存储类型");
        }
    }
}
