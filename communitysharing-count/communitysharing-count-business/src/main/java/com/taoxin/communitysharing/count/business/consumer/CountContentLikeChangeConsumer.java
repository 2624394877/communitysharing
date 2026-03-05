package com.taoxin.communitysharing.count.business.consumer;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.constant.RedisKeyConstant;
import com.taoxin.communitysharing.count.business.enums.LikeUnlikeContentTypeEnum;
import com.taoxin.communitysharing.count.business.model.dto.AggregationCountLikeUnlikeMQDTO;
import com.taoxin.communitysharing.count.business.model.dto.CountLikeUnlikeContentMqDTO;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_LIKE_OR_UNLIKE + "_" + MQConstant.TOPIC_CONTENT_LIKE_COUNT_CHANGE,
        topic = MQConstant.TOPIC_LIKE_OR_UNLIKE
        // 默认是并发消费
)
public class CountContentLikeChangeConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    // 批量消费的 BufferTrigger，积攒一定数量的消息后批量处理
    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeMessage) // 设置消费函数
            .build();

    @Override
    public void onMessage(String body) {
        // 将消息加入 BufferTrigger，BufferTrigger 会根据设置的条件自动调用 consumeMessage 进行批量处理
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> strings) {
        log.info("==> 聚合消息, size: {}", strings.size());
        log.info("==> 聚合消息, {}", JsonUtil.toJsonString(strings));
        List<CountLikeUnlikeContentMqDTO> countLikeUnlikeContentMqDTOS = strings.stream()
                .map(str -> JsonUtil.parseObject(str, CountLikeUnlikeContentMqDTO.class))
                .toList();
        Map<Long,List<CountLikeUnlikeContentMqDTO>> map = countLikeUnlikeContentMqDTOS.stream()
                .collect(java.util.stream.Collectors.groupingBy(CountLikeUnlikeContentMqDTO::getContentId));
        // 汇总统计
        List<AggregationCountLikeUnlikeMQDTO> aggregationCountLikeUnlikeMQDTOList = Lists.newArrayList();
        for (Map.Entry<Long, List<CountLikeUnlikeContentMqDTO>> entry : map.entrySet()) {
            Long contentId = entry.getKey();
            Long creatorId = null;
            List<CountLikeUnlikeContentMqDTO> list = entry.getValue(); //  同一 contentId 的消息列表
            int likeCount = 0;
            for (CountLikeUnlikeContentMqDTO dto : list) {
                // 通过定义枚举判断类型
                Integer status = dto.getStatus();
                creatorId = dto.getCreatorId();
                LikeUnlikeContentTypeEnum likeUnlikeContentTypeEnum = LikeUnlikeContentTypeEnum.getByCode(status);
                switch (likeUnlikeContentTypeEnum) {
                    case LIKE -> likeCount++; // 点赞 +1
                    case UNLIKE -> likeCount--; // 取消点赞 -1
                }
            }
            AggregationCountLikeUnlikeMQDTO aggregationCountLikeUnlikeMQDTO = AggregationCountLikeUnlikeMQDTO.builder()
                    .contentId(contentId)
                    .creatorId(creatorId)
                    .likeCount(likeCount)
                    .build();
            aggregationCountLikeUnlikeMQDTOList.add(aggregationCountLikeUnlikeMQDTO);
//            countMap.put(entry.getKey(), likeCount); // 一个 contentId 的最终点赞数增量
        }
        log.info("【点赞/取消点赞】 聚合后的统计结果: {}", JsonUtil.toJsonString(aggregationCountLikeUnlikeMQDTOList));

        // 更新redis
        aggregationCountLikeUnlikeMQDTOList.forEach((aggregationCountLikeUnlikeMQDTO) -> {
            String contentKey = RedisKeyConstant.buildCountContentKey(aggregationCountLikeUnlikeMQDTO.getContentId());
            // 先判断 Hash 是否存在 （即 contentId 是否存在），不存在则不处理，避免误操作
            boolean isExisted = redisTemplate.hasKey(contentKey);

            if (isExisted) {
                // 对 Hash 中的 likeTotal 字段进行加减操作
                redisTemplate.opsForHash().increment(contentKey, RedisKeyConstant.FIELD_LIKE_TOTAL, aggregationCountLikeUnlikeMQDTO.getLikeCount());
            }

            String CountUserkey = RedisKeyConstant.buildCountUserKey(aggregationCountLikeUnlikeMQDTO.getCreatorId());
            boolean isUserExisted = redisTemplate.hasKey(CountUserkey);
            if (isUserExisted) { // 用户维度的统计也进行更新
                // 对 Hash 中的 likeTotal 字段进行加减操作
                redisTemplate.opsForHash().increment(CountUserkey, RedisKeyConstant.FIELD_LIKE_TOTAL, aggregationCountLikeUnlikeMQDTO.getLikeCount());
            }
        });

        // 发送MQ，内容点赞数变化入库
        // todo 发送 MQ 消息，让计数服务消费，入库
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(aggregationCountLikeUnlikeMQDTOList))
                .build();
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_CONTENT_LIKE_COUNT_CHANGE_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("【内容点赞数变化入库】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("【内容点赞数变化入库】MQ 发送异常: ", throwable);
            }
        });
    }
}
