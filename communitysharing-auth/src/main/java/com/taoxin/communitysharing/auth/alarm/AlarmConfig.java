package com.taoxin.communitysharing.auth.alarm;

import com.taoxin.communitysharing.auth.alarm.impl.MailAlarmHelper;
import com.taoxin.communitysharing.auth.alarm.impl.SmsAlarmHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @RefreshScope注解： 配置文件热更新，当配置中心的配置发生变化时，标注了 @RefreshScope 的 Bean 会重新加载最新的配置。
 * @主要功能：
 * 1. 当 Nacos 配置中心的配置发生变化时，应用中的配置会自动更新
 * 2. 标注了 @RefreshScope 的 Bean 会在配置变化后重新加载，确保 Bean 使用最新的配置
 * 3. 与 Spring Cloud 的配置管理机制紧密集成，能够无缝地处理配置更新事件。
 * @注：
 * 只有当配置类被 @RefreshScope 修饰时，类中的 Helper() Bean 才会在配置变更时被重新创建；
 * 单独给方法加 @RefreshScope 可能无法正确获取到已更新的 alarmType 属性值
 */
@Configuration
public class AlarmConfig {
    @Bean
    @RefreshScope
    public AlarmInterface Helper(@Value("${alarm.type}") String alarmType) {
        if (StringUtils.equals("sms", alarmType)) {
            return new SmsAlarmHelper();
        } else if (StringUtils.equals("mail", alarmType)) {
            return new MailAlarmHelper();
        } else {
            throw new IllegalArgumentException("不支持的告警方式");
        }
    }
}
