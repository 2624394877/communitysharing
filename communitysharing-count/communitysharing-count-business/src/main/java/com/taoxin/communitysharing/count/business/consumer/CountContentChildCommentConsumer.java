package com.taoxin.communitysharing.count.business.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.enums.CommentLevelEnum;
import com.taoxin.communitysharing.count.business.model.dto.CountPublishCommentMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(1000)
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
        Map<Long, Long> map = countPublishCommentMqDTOS.stream()
                .filter(entity -> Objects.equals(entity.getLevel(), CommentLevelEnum.SECOND_LEVEL.getCode()))
                .collect(Collectors.groupingBy(
                        CountPublishCommentMqDTO::getContentId,
                        Collectors.counting()
                ));
        log.info("【子评论数计数】 二级评论: {}", map);
        map.forEach((contentId, count) -> {
            log.info("【子评论数计数】内容 {} 二级评论数 {}", contentId, count);
        });
    }
}
