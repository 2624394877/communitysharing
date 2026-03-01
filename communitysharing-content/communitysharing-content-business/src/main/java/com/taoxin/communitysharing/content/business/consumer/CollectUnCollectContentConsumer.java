package com.taoxin.communitysharing.content.business.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.content.business.constant.MQConstant;
import com.taoxin.communitysharing.content.business.domain.databaseObject.ContentCollectionDo;
import com.taoxin.communitysharing.content.business.domain.mapper.ContentCollectionDoMapper;
import com.taoxin.communitysharing.content.business.model.dto.ContentCollectUnCollectMQDTO;
import com.taoxin.communitysharing.content.business.model.dto.LikeUnlikeMQDTO;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Component
public class CollectUnCollectContentConsumer {
    @Value("${rocketmq.name-server}")
    private String nameServer;
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private ContentCollectionDoMapper contentCollectionDoMapper;

    private DefaultMQPushConsumer consumer;

    @Bean("CollectUnCollectContentConsumer")
    public DefaultMQPushConsumer mqPushConsumer() throws MQClientException {
        String GroupName = "communitysharing_group_" + MQConstant.TOPIC_COLLECT_OR_UNCOLLECT;
        consumer =  new DefaultMQPushConsumer(GroupName);
        consumer.setNamesrvAddr(nameServer);
        consumer.subscribe(MQConstant.TOPIC_COLLECT_OR_UNCOLLECT, "*");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setMaxReconsumeTimes(3);
        consumer.setConsumeMessageBatchMaxSize(30);
        consumer.setPullInterval(1000);
        consumer.registerMessageListener((MessageListenerConcurrently)(msgs, context) -> {
            log.info("【收藏/取消收藏】 收到消息: size={}", msgs.size());
            try {
                List<ContentCollectUnCollectMQDTO> collectUnCollectMQDTOS = Lists.newArrayList();
                msgs.forEach(msg -> {
                    String body = new String(msg.getBody());
                    ContentCollectUnCollectMQDTO collectUnCollectMQDTO = JsonUtil.parseObject(body, ContentCollectUnCollectMQDTO.class);
                    log.info("【收藏/取消收藏】 消息体: {}", collectUnCollectMQDTO);
                    collectUnCollectMQDTOS.add(collectUnCollectMQDTO);
                });
                Map<Long, List<ContentCollectUnCollectMQDTO>> groupMap = collectUnCollectMQDTOS.stream()
                        .collect(Collectors.groupingBy(ContentCollectUnCollectMQDTO::getUserId));
                List<ContentCollectUnCollectMQDTO> finalOperations = groupMap.entrySet().stream()
                        .flatMap(userOperations ->{
                            Map<Long, List<ContentCollectUnCollectMQDTO>> contentIdMap = userOperations.getValue().stream()
                                    .collect(Collectors.groupingBy(ContentCollectUnCollectMQDTO::getContentId));
                            return contentIdMap.entrySet().stream()
                                    .filter(entry -> {
                                        List<ContentCollectUnCollectMQDTO> operations = entry.getValue();
                                        int size = operations.size(); // 获取操作数量
                                        // 根据奇偶性判断是否需要处理
                                        if (size % 2 == 0) {
                                            // 偶数次操作：最终状态抵消，无需写入
                                            return false;
                                        } else {
                                            // 奇数次操作：保留最后一次操作
                                            return true;
                                        }
                                    })
                                    .map(entry -> {
                                        List<ContentCollectUnCollectMQDTO> operations = entry.getValue();
                                        return operations.get(operations.size() - 1);
                                    });
                        }).toList();
                if (CollUtil.isNotEmpty(finalOperations)) {
                    List<ContentCollectionDo> contentCollectionDos = finalOperations.stream()
                            .map(status -> ContentCollectionDo.builder()
                                    .userId(status.getUserId())
                                    .contentId(status.getContentId())
                                    .status(status.getStatus())
                                    .createTime(status.getCreateTime())
                                    .build()
                            ).toList();
                    contentCollectionDoMapper.batchInsertOrUpdate(contentCollectionDos);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        consumer.start();
        return consumer;
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(consumer)) {
            try {
                consumer.shutdown();
            } catch (Exception e) {
                log.error("【收藏/取消收藏】消费者关闭失败", e);
            }
        }
    }
}
