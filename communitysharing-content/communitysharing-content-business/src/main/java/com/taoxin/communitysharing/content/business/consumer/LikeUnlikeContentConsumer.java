package com.taoxin.communitysharing.content.business.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.content.business.constant.MQConstant;
import com.taoxin.communitysharing.content.business.domain.databaseObject.ContentLikeDo;
import com.taoxin.communitysharing.content.business.domain.mapper.ContentLikeDoMapper;
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
public class LikeUnlikeContentConsumer {
    @Value("${rocketmq.name-server}")
    private String nameServer;
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private ContentLikeDoMapper contentLikeDoMapper;

    private DefaultMQPushConsumer consumer;

    @Bean("LikeUnlikeContentConsumer")
    public DefaultMQPushConsumer mqPushConsumer() throws MQClientException {
        String GroupName = "communitysharing_group_" + MQConstant.TOPIC_LIKE_OR_UNLIKE;
        // 创建一个新的 DefaultMQPushConsumer 实例，并指定消费者的消费组名
        consumer = new DefaultMQPushConsumer(GroupName);
        // 设置 NameServer 的地址
        consumer.setNamesrvAddr(nameServer);
        // 订阅指定的主题，并设置主题的订阅规则（"*" 表示订阅所有标签的消息）
        consumer.subscribe(MQConstant.TOPIC_LIKE_OR_UNLIKE, "*");
        // 设置消费者消费消息的起始位置，如果队列中没有消息，则从最新的消息开始消费。
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        // 设置消息消费模式，这里使用集群模式 (CLUSTERING)
        consumer.setMessageModel(MessageModel.CLUSTERING);
        // 最大重试次数, 以防消息重试过多次仍然没有成功，避免消息卡在消费队列中。
        consumer.setMaxReconsumeTimes(3);
        // 设置每批次消费的最大消息数量，这里设置为 30，表示每次拉取时最多消费 30 条消息
        consumer.setConsumeMessageBatchMaxSize(30);
        // 设置拉取间隔, 单位毫秒
        consumer.setPullInterval(1000);

        consumer.registerMessageListener((MessageListenerConcurrently)(msgs, context) -> {
            log.info("【点赞/取消点赞】 收到消息: size={}", msgs.size());
            try {
                rateLimiter.acquire();
                // todo 幂等性: 通过联合唯一索引保证

                // 消息体 Json 字符串转 DTO
                List<LikeUnlikeMQDTO> likeUnlikeMQDTOS = Lists.newArrayList();
                msgs.forEach(msg -> {
                    String body = new String(msg.getBody());
                    log.info("【点赞/取消点赞】消息体: {}", body);
                    likeUnlikeMQDTOS.add(JsonUtil.parseObject(body, LikeUnlikeMQDTO.class));
                });
                // 1. 内存级操作合并
                // 按用户 ID 进行分组
                Map<Long, List<LikeUnlikeMQDTO>> groupMap = likeUnlikeMQDTOS.stream()
                        .collect(Collectors.groupingBy(LikeUnlikeMQDTO::getUserId));
                // 对每个用户的操作按 noteId 二次分组，并过滤合并
                List<LikeUnlikeMQDTO> finalOperations = groupMap.entrySet().stream()
                        .flatMap(userOperations ->{
                            // 按 noteId 分组
                            Map<Long, List<LikeUnlikeMQDTO>> contentOperations = userOperations.getValue().stream()
                                    .collect(Collectors.groupingBy(LikeUnlikeMQDTO::getContentId));
                            // 处理每个 noteId 的分组
                            return contentOperations.entrySet().stream()
                                    .filter(entry ->{
                                        List<LikeUnlikeMQDTO> operations = entry.getValue();
                                        int size = operations.size();
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
                                        List<LikeUnlikeMQDTO> operations = entry.getValue();
                                        return operations.get(operations.size() - 1);
                                    });
                        }).toList();
                // TODO: 2. 批量写入数据库
                if (CollUtil.isNotEmpty(finalOperations)) {
                    // DTO 转 DO
                    List<ContentLikeDo> contentLikeDos = finalOperations.stream()
                            .map(operation ->
                                ContentLikeDo.builder()
                                        .userId(operation.getUserId())
                                        .contentId(operation.getContentId())
                                        .createTime(operation.getCreateTime())
                                        .status(operation.getStatus())
                                        .build()
                            ).toList();
                    int count = contentLikeDoMapper.batchInsertOrUpdate(contentLikeDos);
                    if (count <= 0) log.error("【点赞/取消点赞】批量写入数据库失败：{}", contentLikeDos);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                log.error("【点赞/取消点赞】消息解析失败: {}", e);
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
                log.error("【点赞/取消点赞】消费者关闭失败", e);
            }
        }
    }
}
