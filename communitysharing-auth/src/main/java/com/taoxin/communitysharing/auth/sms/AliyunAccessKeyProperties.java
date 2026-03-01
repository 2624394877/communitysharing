package com.taoxin.communitysharing.auth.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * 阿里云短信服务配置属性
 */
@ConfigurationProperties(prefix = "aliyun") // 配置属性前缀
@Component // 创建组件
@Data // 创建Getter和Setter方法
public class AliyunAccessKeyProperties {
    private String accessKeyId;
    private String accessKeySecret;
}
