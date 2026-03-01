package com.taoxin.communitysharing.content.business.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.taoxin.communitysharing.content.business.constant.MQConstant;
import com.taoxin.communitysharing.content.business.domain.databaseObject.ContentCollectionDo;
import com.taoxin.communitysharing.content.business.domain.mapper.ContentCollectionDoMapper;
import com.taoxin.communitysharing.content.business.model.dto.ContentCollectUnCollectMQDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_COLLECT_OR_UNCOLLECT,
        topic = MQConstant.TOPIC_COLLECT_OR_UNCOLLECT + "a",
        consumeMode = ConsumeMode.ORDERLY // 顺序消费
)
public class CollectUnCollectContentConsumer2 implements RocketMQListener<Message> {
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private ContentCollectionDoMapper contentCollectionDoMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Message message) {
        rateLimiter.acquire(); // 获取令牌，进行限流
        String body = new String(message.getBody()); // 获取消息体
        String tag = message.getTags(); // 获取消息标签
        if (Objects.equals(tag, MQConstant.TAG_COLLECT)) {
            // 处理收藏笔记的消息
            try {
                handleCollectContentTagMessage(body);
            } catch (Exception e) {
                log.error("【收藏/取消收藏】 处理收藏消息失败: body={}, error={}", body, e.getMessage(), e);
                return;
            }
            log.info("【收藏/取消收藏】 收到消息: tag={}, body={}, messageId={}",tag, body, message.getBuyerId());
        } else if (Objects.equals(tag, MQConstant.TAG_UNCOLLECT)) {
            // 处理取消收藏笔记的消息
            try {
                handleUnCollectContentTagMessage(body);
            } catch (Exception e) {
                log.error("【收藏/取消收藏】 处理取消收藏消息失败: body={}, error={}", body, e.getMessage(), e);
                return;
            }
            log.info("【收藏/取消收藏】 收到消息: tag={}, body={}",tag, body);
        }
    }

    private void handleUnCollectContentTagMessage(String body) {
        ContentCollectUnCollectMQDTO contentCollectUnCollectMQDTO = JSON.parseObject(body, ContentCollectUnCollectMQDTO.class);
        if (Objects.isNull(contentCollectUnCollectMQDTO)) return;
        ContentCollectionDo contentCollectionDo = ContentCollectionDo.builder()
                .userId(contentCollectUnCollectMQDTO.getUserId())
                .contentId(contentCollectUnCollectMQDTO.getContentId())
                .createTime(contentCollectUnCollectMQDTO.getCreateTime())
                .status(contentCollectUnCollectMQDTO.getStatus())
                .build();
        log.info("【收藏/取消收藏】 处理取消收藏消息: userId={}", contentCollectionDo.getUserId());
        int count = contentCollectionDoMapper.insertOrUpdate(contentCollectionDo);
        if (count<0) return; // 插入或更新失败
        org.springframework.messaging.Message message = MessageBuilder.withPayload(body).build();
        // 发送计数消息到 RocketMQ
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("【取消收藏】 发送计数消息成功: topic={}, tags={}, body={}", MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE, MQConstant.TAG_UNCOLLECT, body);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("【取消收藏】 发送计数消息失败: topic={}, tags={}, body={}, error={}", MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE, MQConstant.TAG_UNCOLLECT, body, throwable.getMessage(), throwable);
            }
        });
    }

    /**
     * 处理收藏信息
     * @param body
     */
    private void handleCollectContentTagMessage(String body) {
        ContentCollectUnCollectMQDTO contentCollectUnCollectMQDTO = JSON.parseObject(body, ContentCollectUnCollectMQDTO.class);
        if (Objects.isNull(contentCollectUnCollectMQDTO)) return;
        ContentCollectionDo contentCollectionDo = ContentCollectionDo.builder()
                .userId(contentCollectUnCollectMQDTO.getUserId())
                .contentId(contentCollectUnCollectMQDTO.getContentId())
                .createTime(contentCollectUnCollectMQDTO.getCreateTime())
                .status(contentCollectUnCollectMQDTO.getStatus())
                .build();
        log.info("【收藏/取消收藏】 处理收藏消息: userId={}, contentId={}, status={}", contentCollectionDo.getUserId(), contentCollectionDo.getContentId(), contentCollectionDo.getStatus());
        int count = contentCollectionDoMapper.insertOrUpdate(contentCollectionDo);
        if (count<0) return; // 插入或更新失败
        org.springframework.messaging.Message message = MessageBuilder.withPayload(body).build();
        // 发送计数消息到 RocketMQ
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("【收藏】 发送计数消息成功: topic={}, tags={}, body={}", MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE, MQConstant.TAG_COLLECT, body);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("【收藏】 发送计数消息失败: topic={}, tags={}, body={}, error={}", MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE, MQConstant.TAG_COLLECT, body, throwable.getMessage(), throwable);
            }
        });
    }
}
