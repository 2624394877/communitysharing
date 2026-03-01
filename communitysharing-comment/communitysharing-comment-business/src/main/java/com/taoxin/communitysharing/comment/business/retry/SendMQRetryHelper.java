package com.taoxin.communitysharing.comment.business.retry;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SendMQRetryHelper {
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private RetryTemplate retryTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;


    @Retryable (
        retryFor = { Exception.class }, // 默认重试的异常类型
        maxAttempts = 3, // 最大重试次数
        backoff = @Backoff(delay = 1000, multiplier = 2) // 延迟时间，乘数
    )
    public void sendMQ(String topic, String Json, String tag) {
        log.info("========= 【{}发送MQ】 发送开始: {}",tag, Json);
        Message<String> message = MessageBuilder.withPayload(Json).build();
        rocketMQTemplate.asyncSend(topic, message,new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========= 【{}发送MQ】 发送成功: {}",tag, sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("========= 【{}发送MQ】 发送失败: {}",tag, throwable);
                handleRetry(topic, message);
            }
        });
    }

    private void handleRetry(String topic, Message<String> message) {
        taskExecutor.submit(() -> {
            try {
                retryTemplate.execute((RetryCallback<Void, RuntimeException>) context ->{
                    log.info("【发送MQ】开始重试 topic: {}, message: {}", topic, message);
                    rocketMQTemplate.syncSend(topic, message);
                    return null;
                });
            } catch (Exception e) {
                asyncSendMessageFallback(e, topic, message.getPayload());
            }
        });
    }

    @Recover
    public void asyncSendMessageFallback(Exception e, String topic, String Json) {
        log.error("【发送MQ】发送失败 topic: {}, message: {}, 异常信息: {}", topic, Json, e);
        // todo 重试失败后的处理逻辑
    }
}
