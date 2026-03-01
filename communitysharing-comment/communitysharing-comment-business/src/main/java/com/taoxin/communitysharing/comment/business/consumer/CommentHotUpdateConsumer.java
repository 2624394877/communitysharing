package com.taoxin.communitysharing.comment.business.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.taoxin.communitysharing.comment.business.constant.MQConstant;
import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentDo;
import com.taoxin.communitysharing.comment.business.domain.mapper.CommentDoMapper;
import com.taoxin.communitysharing.comment.business.model.bo.CommentHotBo;
import com.taoxin.communitysharing.comment.business.utils.HotArithmetic;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_COUNT_HEAT_UPDATE,
        topic = MQConstant.TOPIC_COUNT_HEAT_UPDATE
)
public class CommentHotUpdateConsumer implements RocketMQListener<String> {
    @Resource
    private RateLimiter rateLimiter;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;
    @Resource
    private CommentDoMapper commentDoMapper;

    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(500) // 多少条聚合一次
            .linger(java.time.Duration.ofMillis(500)) // 500 ms聚合一次
            .setConsumerEx(this::consumeMessage)
            .build();

    @Override
    public void onMessage(String body) {
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> strings) {
        log.info("【评论热度更新】 聚合消息, size: {}", strings.size());
        log.info("【评论热度更新】 聚合消息, {}", JsonUtil.toJsonString(strings));
        rateLimiter.acquire(strings.size());
        taskExecutor.submit(()->{
            Set<Long> commentIds = Sets.newHashSet();
            strings.forEach(body -> {
                try {
                    Set<Long> commentIdSet = JsonUtil.parseSet(body, Long.class);
                    commentIds.addAll(commentIdSet);
                } catch (Exception e) {
                    log.error("【评论热度更新】 解析消息失败： {}", e);
                }
            });
            log.info("【评论热度更新】 解析的聚合消息, commentIds: {}", commentIds);
            // todo 更新
            // 批量查询评论
            List<CommentDo> commentDos = commentDoMapper.selectByCommentIds(commentIds.stream().toList());
            log.info("【评论热度更新】 批量查询的评论, commentDos: {}", commentDos);
            // 评论id
            List<Long> ids = Lists.newArrayList();
            // 热度实体
            List<CommentHotBo> commentHeatList = Lists.newArrayList();

            commentDos.forEach(commentDo -> {
                Long commentId = commentDo.getId();
                Long replyTotal = commentDo.getChildCommentTotal();
                Long likeTotal = commentDo.getLikeTotal();
                // 调用计算热度值
                log.info("【评论热度更新】 计算热度值, commentId: {}, likeTotal: {}, replyTotal: {}", commentId, likeTotal, replyTotal);
                BigDecimal heat = HotArithmetic.calculateHeat(likeTotal, replyTotal);
                ids.add(commentId);
                commentHeatList.add(CommentHotBo.builder()
                        .commentId(commentId)
                        .hot(heat.doubleValue())
                        .build());
            });
            commentDoMapper.bacthUpdateCommentHeat(ids, commentHeatList);
        });
    }
}
