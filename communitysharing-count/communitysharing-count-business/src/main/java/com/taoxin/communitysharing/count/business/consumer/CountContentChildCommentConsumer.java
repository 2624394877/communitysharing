package com.taoxin.communitysharing.count.business.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.constant.RedisKeyConstant;
import com.taoxin.communitysharing.count.business.domain.mapper.CommentDoMapper;
import com.taoxin.communitysharing.count.business.enums.CommentLevelEnum;
import com.taoxin.communitysharing.count.business.model.dto.CountPublishCommentMqDTO;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_child" + MQConstant.TOPIC_COUNT_COMMENT,
        topic = MQConstant.TOPIC_COUNT_COMMENT
)
public class CountContentChildCommentConsumer implements RocketMQListener<String> {
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private CommentDoMapper commentDoMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(500)
            .linger(java.time.Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeMessage)
            .build();

    @Override
    public void onMessage(String body) {
        log.info("【子评论数计数】 聚合消息, size: {}", body);
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> strings) {
        log.info("【子评论数计数】 聚合消息, size: {}", strings.size());
        log.info("【子评论数计数】 聚合消息, {}", JsonUtil.toJsonString(strings));
        rateLimiter.acquire(strings.size());
        List<CountPublishCommentMqDTO> countPublishCommentMqDTOS = Lists.newArrayList();
        strings.forEach(str -> {
            try {
                log.info("【子评论数计数】 解析消息： {}", str);
                List<CountPublishCommentMqDTO> list = JsonUtil.parseList(str, CountPublishCommentMqDTO.class);
                countPublishCommentMqDTOS.addAll(list);
            } catch (Exception e) {
                log.error("【子评论数计数】 解析消息失败： {}", e);
            }
        });

        // 按笔记 ID 进行分组
        Map<Long, Integer> map = countPublishCommentMqDTOS.stream()
                .filter(entity -> Objects.equals(entity.getLevel(), CommentLevelEnum.SECOND_LEVEL.getCode()))
                .collect(Collectors.groupingBy(
                        CountPublishCommentMqDTO::getParentId,
                        Collectors.summingInt(e-> 1)
                ));
        log.info("【子评论数计数】 二级评论: {}", map);
        map.forEach((parentId, count) -> {
            // 更新redisSet
            String prefixKey = RedisKeyConstant.getCountCommentKeyPrefix(parentId);
            boolean isExist = redisTemplate.hasKey(prefixKey);
            if (isExist) {
                // 累加到缓存
                redisTemplate.opsForHash().increment(prefixKey, RedisKeyConstant.CHILD_COMMENT_TOTAL, count);
            }
            log.info("【子评论数计数】父评论 {} 二级评论数 {}", parentId, count);
            commentDoMapper.updateChildCommentTotal(parentId, count);
        });

        // 发送评论热度更新消息
        Set<Long> commentIds = countPublishCommentMqDTOS.stream()
                .map(CountPublishCommentMqDTO::getParentId)
                .collect(Collectors.toSet());
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(commentIds)).build();

        rocketMQTemplate.asyncSend(MQConstant.TOPIC_COUNT_HEAT_UPDATE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("【子评论数计数】 发送评论热度更新消息成功： {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("【子评论数计数】 发送评论热度更新消息失败： {}", throwable);
            }
        });
    }
}
