package com.taoxin.communitysharing.count.business.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.constant.RedisKeyConstant;
import com.taoxin.communitysharing.count.business.domain.mapper.ContentCountDoMapper;
import com.taoxin.communitysharing.count.business.model.dto.CountPublishCommentMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_COUNT_COMMENT,
        topic = MQConstant.TOPIC_COUNT_COMMENT
)
public class CountContentCommentConsumer implements RocketMQListener<String> {
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private ContentCountDoMapper contentCountDoMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(1000)
            .linger(java.time.Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeMessage)
            .build();

    @Override
    public void onMessage(String body) {
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> strings) {
        log.info("【评论数计数】 聚合消息, size: {}", strings.size());
        log.info("【评论数计数】 聚合消息, {}", JsonUtil.toJsonString(strings));
        rateLimiter.acquire(strings.size());
        List<CountPublishCommentMqDTO> countPublishCommentMqDTOS = Lists.newArrayList();
        strings.forEach(str -> {
            try {
                log.info("【评论数计数】 解析消息： {}", str);
                List<CountPublishCommentMqDTO> list = JsonUtil.parseList(str, CountPublishCommentMqDTO.class);
                countPublishCommentMqDTOS.addAll(list);
            } catch (Exception e) {
                log.error("【评论数计数】 解析消息失败： {}", e);
            }
        });

        // 按笔记 ID 进行分组
        Map<Long, List<CountPublishCommentMqDTO>> map = countPublishCommentMqDTOS.stream()
                .collect(Collectors.groupingBy(CountPublishCommentMqDTO::getContentId));
        for (Map.Entry<Long, List<CountPublishCommentMqDTO>> entry : map.entrySet()) {
            Long contentId = entry.getKey();
            int count = entry.getValue().size(); // 笔记 ID 对应的评论数

            String contentCountKey = RedisKeyConstant.buildCountContentKey(contentId);
            boolean exists = redisTemplate.hasKey(contentCountKey);
            if (exists) {
                redisTemplate.opsForHash().increment(contentCountKey, RedisKeyConstant.FIELD_COMMENT_TOTAL, count);
            }

            if (count > 0) {
                log.info("【评论数计数】 内容 {} 评论数 {}", contentId, count);
                contentCountDoMapper.insertOrUpdateCommentTotalByContentId(count,contentId);
            }
        }
    }
}
