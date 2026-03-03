package com.taoxin.communitysharing.count.business.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.constant.RedisKeyConstant;
import com.taoxin.communitysharing.count.business.enums.LikeUnlikeCommentTypeEnum;
import com.taoxin.communitysharing.count.business.model.dto.AggregationCountLikeUnlikeCommentMqDTO;
import com.taoxin.communitysharing.count.business.model.dto.LikeUnLikeCommentMqDTO;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_count_" + MQConstant.TOPIC_COMMENT_LIKE,
        topic = MQConstant.TOPIC_COMMENT_LIKE
)
public class CountCommentLikeConsumer implements RocketMQListener<String>  {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(500)
            .linger(java.time.Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeMessage)
            .build();

    @Override
    public void onMessage(String s) {
        log.info("【评论点赞数计数】 收到消息, size: {}", s);
        bufferTrigger.enqueue(s);
    }

    private void consumeMessage(List<String> strings) {
        log.info("【评论点赞数计数】 聚合消息, size: {}", strings.size());
        log.info("【评论点赞数计数】 聚合消息, {}", JsonUtil.toJsonString(strings));

        // List<String> 转 List<LikeUnLikeCommentMqDTO>
        List<LikeUnLikeCommentMqDTO> countLikeUnlikeCommentMqDTOS = strings.stream()
                .map(body -> JsonUtil.parseObject(body, LikeUnLikeCommentMqDTO.class)).toList();

        // 按评论 ID 进行分组
        Map<Long, List<LikeUnLikeCommentMqDTO>> groupMap = countLikeUnlikeCommentMqDTOS.stream()
                .collect(Collectors.groupingBy(LikeUnLikeCommentMqDTO::getCommentId));

        // 按组汇总数据，统计出最终的计数
        // 最终操作的计数对象
        List<AggregationCountLikeUnlikeCommentMqDTO> countList = Lists.newArrayList();

        for (Map.Entry<Long, List<LikeUnLikeCommentMqDTO>> entry : groupMap.entrySet()) {
            // 评论 ID
            Long commentId = entry.getKey();

            List<LikeUnLikeCommentMqDTO> list = entry.getValue();
            // 最终的计数值，默认为 0
            int finalCount = 0;
            for (LikeUnLikeCommentMqDTO countLikeUnlikeCommentMqDTO : list) {
                // 获取操作类型
                Integer type = countLikeUnlikeCommentMqDTO.getType();

                // 根据操作类型，获取对应枚举
                LikeUnlikeCommentTypeEnum likeUnlikeCommentTypeEnum = LikeUnlikeCommentTypeEnum.valueOf(type);

                // 若枚举为空，跳到下一次循环
                if (Objects.isNull(likeUnlikeCommentTypeEnum)) continue;

                switch (likeUnlikeCommentTypeEnum) {
                    case LIKE -> finalCount += 1; // 如果为点赞操作，点赞数 +1
                    case UNLIKE -> finalCount -= 1; // 如果为取消点赞操作，点赞数 -1
                }
            }
            // 将分组后统计出的最终计数，存入 countList 中
            countList.add(AggregationCountLikeUnlikeCommentMqDTO.builder()
                    .commentId(commentId)
                    .count(finalCount)
                    .build());
        }
        log.info("## 【评论点赞数】聚合后的计数数据: {}", JsonUtil.toJsonString(countList));

        // 更新 Redis
        countList.forEach(item -> {
            // 评论 ID
            Long commentId = item.getCommentId();
            // 聚合后的计数
            Integer count = item.getCount();

            // Redis 中评论计数 Hash Key
            String countCommentRedisKey = RedisKeyConstant.getCountCommentKeyPrefix(commentId);
            // 判断 Redis 中 Hash 是否存在
            boolean isCountCommentExisted = redisTemplate.hasKey(countCommentRedisKey);

            // 若存在才会更新
            // (因为缓存设有过期时间，考虑到过期后，缓存会被删除，这里需要判断一下，存在才会去更新，而初始化工作放在查询计数来做)
            if (isCountCommentExisted) {
                // 对目标用户 Hash 中的点赞数字段进行计数操作
                redisTemplate.opsForHash().increment(countCommentRedisKey, RedisKeyConstant.FIELD_LIKE_TOTAL, count);
            }
        });

        // 发送 MQ, 评论点赞数据落库
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(countList))
                .build();
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_COUNT_COMMENT_LIKE_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========= 【评论点赞数落库】 ====> {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("========= 【评论点赞数落库】 ====> {}", throwable);
            }
        });
    }
}
