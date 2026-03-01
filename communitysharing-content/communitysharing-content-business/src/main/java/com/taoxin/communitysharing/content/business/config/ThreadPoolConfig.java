package com.taoxin.communitysharing.content.business.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    /**
     * 创建一个名为"threadPoolTaskExecutor"的线程池任务执行器Bean，供Spring框架管理和注入使用。
     */
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 设置核心线程数
        executor.setMaxPoolSize(50); // 设置最大线程数
        executor.setQueueCapacity(200); // 设置队列容量
        executor.setKeepAliveSeconds(30); // 设置线程空闲时间
        executor.setThreadNamePrefix("ContentExecutor-"); // 设置线程名称前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 设置拒绝策略
        executor.setWaitForTasksToCompleteOnShutdown(true); // 设置线程池关闭时是否等待所有任务完成
        executor.setAwaitTerminationSeconds(60); // 设置等待时间

        executor.initialize(); // 初始化线程池

        return executor;
    }

}
