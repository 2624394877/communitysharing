package com.taoxin.communitysharing.count.business.consumer;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.constant.RedisKeyConstant;
import com.taoxin.communitysharing.count.business.enums.CollectUnCollectTypeEnum;
import com.taoxin.communitysharing.count.business.model.dto.AggregationCountCollectUnCollectMQDTO;
import com.taoxin.communitysharing.count.business.model.dto.CountCollectUnCollectMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_count_" + MQConstant.TOPIC_COLLECT_OR_UNCOLLECT,
        topic = MQConstant.TOPIC_COLLECT_OR_UNCOLLECT
)
public class CountContentCollectChangeConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一次最多聚合 1000 条
            .linger(java.time.Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeMessage) // 设置消费函数
            .build();

    @Override
    public void onMessage(String body) {
        bufferTrigger.enqueue(body); // 将消息加入 BufferTrigger，BufferTrigger 会根据设置的条件自动调用 consumeMessage 进行批量处理
    }

    private void consumeMessage(List<String> strings) {
        log.info("==> 聚合消息, size: {}", strings.size());
        log.info("==> 聚合消息, {}", JsonUtil.toJsonString(strings));
        List<CountCollectUnCollectMqDTO> countCollectUnCollectMqDTOS = strings.stream()
                .map(str -> JsonUtil.parseObject(str, CountCollectUnCollectMqDTO.class))
                .toList();
        // 按 contentId 分组
        Map<Long, List<CountCollectUnCollectMqDTO>> map = countCollectUnCollectMqDTOS.stream()
                .collect(java.util.stream.Collectors.groupingBy(CountCollectUnCollectMqDTO::getContentId));
        // TODO: 这里可以对每个 contentId 的消息进行批量处理，比如更新 Redis 中的收藏数等
        List<AggregationCountCollectUnCollectMQDTO> aggregationCountCollectUnCollectMQDTOS = Lists.newArrayList();
        for (Map.Entry<Long, List<CountCollectUnCollectMqDTO>> entry : map.entrySet()) {
            Long contentId = entry.getKey();
            Long creatorId = null;
            List<CountCollectUnCollectMqDTO> list = entry.getValue();
            int CollectCount = 0; // 统计收藏数
            for (CountCollectUnCollectMqDTO dto : list) {
                Integer status = dto.getStatus();
                creatorId = dto.getCreatorId();
                CollectUnCollectTypeEnum collectUnCollectTypeEnum = CollectUnCollectTypeEnum.getByCode(status); // 根据 status 获取枚举
                log.info("==> 收藏/取消收藏, contentId={}, status={}", contentId, status);
                if (collectUnCollectTypeEnum == CollectUnCollectTypeEnum.COLLECT) {
                    CollectCount++;
                } else if (collectUnCollectTypeEnum == CollectUnCollectTypeEnum.UNCOLLECTED) {
                    CollectCount--;
                }
            }
            AggregationCountCollectUnCollectMQDTO aggregationCountCollectUnCollectMQDTO = AggregationCountCollectUnCollectMQDTO.builder()
                    .contentId(contentId)
                    .creatorId(creatorId)
                    .collectCount(CollectCount)
                    .build();
            aggregationCountCollectUnCollectMQDTOS.add(aggregationCountCollectUnCollectMQDTO);
//            countMap.put(entry.getKey(), CollectCount); // contentId -> 收藏数增量
        }
        log.info("==> 聚合结果, countMap: {}", JsonUtil.toJsonString(aggregationCountCollectUnCollectMQDTOS));

        // 更新redis
        aggregationCountCollectUnCollectMQDTOS.forEach((aggregationCountCollectUnCollectMQDTO) -> {
            String contentKey = RedisKeyConstant.buildCountContentKey(aggregationCountCollectUnCollectMQDTO.getContentId());
            // 先判断 Hash 是否存在 （即 contentId 是否存在），不存在则不处理，避免误操作
            boolean isExisted = redisTemplate.hasKey(contentKey);

            if (isExisted) {
                // 对 Hash 中的 likeTotal 字段进行加减操作
                redisTemplate.opsForHash().increment(contentKey, RedisKeyConstant.FIELD_COLLECT_TOTAL, aggregationCountCollectUnCollectMQDTO.getCollectCount());
            }
        });
        // todo 发送 MQ 消息，让计数服务消费，入库
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(aggregationCountCollectUnCollectMQDTOS))
                .build();
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("【收藏/取消收藏】 发送计数落库消息成功: topic={}, body={}", MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE_2_DB, JsonUtil.toJsonString(aggregationCountCollectUnCollectMQDTOS));
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("【收藏/取消收藏】 发送计数落库消息失败: topic={}, body={}, error={}", MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE_2_DB, JsonUtil.toJsonString(aggregationCountCollectUnCollectMQDTOS), throwable.getMessage(), throwable);
            }
        });
    }
}
