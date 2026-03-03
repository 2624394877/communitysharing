package com.taoxin.communitysharing.count.business.consumer;

import cn.hutool.core.collection.CollUtil;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.domain.mapper.CommentDoMapper;
import com.taoxin.communitysharing.count.business.model.dto.AggregationCountLikeUnlikeCommentMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_COUNT_COMMENT_LIKE_2_DB,
        topic = MQConstant.TOPIC_COUNT_COMMENT_LIKE_2_DB
)
public class CountCommentLike2DBConsumer implements RocketMQListener<String> {
    @Resource
    private CommentDoMapper commentDoMapper;

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(500) // 多少条聚合一次
            .linger(java.time.Duration.ofMillis(500)) // 500 ms聚合一次
            .setConsumerEx(this::consumeMessage)
            .build();

    @Override
    public void onMessage(String s) {
        bufferTrigger.enqueue(s);
    }

    private void consumeMessage(List<String> strings) {
        log.info("【评论点赞数更新】 聚合消息, size: {}", strings.size());
        log.info("【评论点赞数更新】 聚合消息, {}", strings);
        List<AggregationCountLikeUnlikeCommentMqDTO> countList = Lists.newArrayList();
        strings.forEach(item -> {
            try {
                log.info("【评论点赞数更新】 解析, {}", item);
                List<AggregationCountLikeUnlikeCommentMqDTO> aggregationCountLikeUnlikeCommentMqDTOList = JsonUtil.parseList(item, AggregationCountLikeUnlikeCommentMqDTO.class);
                countList.addAll(aggregationCountLikeUnlikeCommentMqDTOList);
            } catch (Exception e) {
                log.error("【评论点赞数更新】 解析失败, {}", e);
            }
        });

        if (CollUtil.isNotEmpty(countList)) {
            // 更新评论点赞数
            countList.forEach(item -> {
                Long commentId = item.getCommentId();
                Integer count = item.getCount();

                commentDoMapper.updateLikeTotalByCommentId(count, commentId);
            });
        }
    }
}
