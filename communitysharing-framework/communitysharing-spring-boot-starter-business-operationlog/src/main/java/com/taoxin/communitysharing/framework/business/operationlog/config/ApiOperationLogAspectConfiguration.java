package com.taoxin.communitysharing.framework.business.operationlog.config;

import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration // 该注解将该类标记为自动配置类
public class ApiOperationLogAspectConfiguration {

    @Bean // 该注解将该方法返回的Bean注册为Spring Bean; 当定义自动配置类时，Spring会自动扫描该类中的所有方法，并调用这些方法来创建Bean。
    public ApiOperationLogAspect apiOperationLogAspect() {
        return new ApiOperationLogAspect();
    }
}
