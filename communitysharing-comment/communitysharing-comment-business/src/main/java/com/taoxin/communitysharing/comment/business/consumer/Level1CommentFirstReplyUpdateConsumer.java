package com.taoxin.communitysharing.comment.business.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.comment.business.constant.CommentContentKeyConstant;
import com.taoxin.communitysharing.comment.business.constant.MQConstant;
import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentDo;
import com.taoxin.communitysharing.comment.business.domain.mapper.CommentDoMapper;
import com.taoxin.communitysharing.comment.business.enums.CommentLevelEnum;
import com.taoxin.communitysharing.comment.business.model.dto.CountPublishCommentMqDTO;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_first_comment" + MQConstant.TOPIC_COUNT_COMMENT,
        topic = MQConstant.TOPIC_COUNT_COMMENT
)
public class Level1CommentFirstReplyUpdateConsumer implements RocketMQListener<String> {
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;
    @Resource
    private CommentDoMapper commentDoMapper;

    private final BufferTrigger<String> trigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(10000)
            .batchSize(1000)
            .linger(java.time.Duration.ofSeconds(1))
            .setConsumerEx(this::consumeMessage)
            .build();

    @Override
    public void onMessage(String body) {
        trigger.enqueue(body);
    }

    private void consumeMessage(List<String> strings) {
        log.info("【一级评论首次回复更新】 聚合消息, size: {}", strings.size());
        log.info("【一级评论首次回复更新】 聚合消息, {}", JsonUtil.toJsonString(strings));
        rateLimiter.acquire(strings.size());
        List<CountPublishCommentMqDTO> countPublishCommentMqDTOS = Lists.newArrayList();
        strings.forEach(str -> {
            try {
                log.info("【一级评论首次回复更新】 解析消息： {}", str);
                List<CountPublishCommentMqDTO> countPublishCommentMqDTOList = JsonUtil.parseList(str, CountPublishCommentMqDTO.class);
                countPublishCommentMqDTOS.addAll(countPublishCommentMqDTOList);
            } catch (Exception e) {
                log.error("【一级评论首次回复更新】 解析消息异常： {}", str, e);
            }
        });
        List<Long> parentIds = countPublishCommentMqDTOS.stream()
                .filter(countPublishCommentMqDTO -> Objects.equals(countPublishCommentMqDTO.getLevel(), CommentLevelEnum.SECOND_LEVEL.getCode()))
                .map(CountPublishCommentMqDTO::getParentId)
                .distinct() // 去重
                .toList();
        log.info("【一级评论首次回复更新】 批量获取一级评论的父评论id: {}", parentIds);
        if (CollUtil.isEmpty(parentIds)) return;
        // 批量获取redisKey
        List<String> keys = parentIds.stream()
                .map(CommentContentKeyConstant::getReplyCommentId)
                .toList();
        // 批量查询 Redis
        List<Object> replyCommentIds = redisTemplate.opsForValue().multiGet(keys);

        // 提取不在reids中的评论id，表示这些评论可能尚未被回复
        List<Long> notReplyCommentIds = Lists.newArrayList();
        for (int i = 0; i < replyCommentIds.size(); i++) {
            if (Objects.isNull(replyCommentIds.get(i)))
                notReplyCommentIds.add(parentIds.get(i));
        }

        // 再数据库中查询
        if (CollUtil.isNotEmpty(notReplyCommentIds)) {
            // todo 查询数据库
            List<CommentDo> commentDos = commentDoMapper.selectByCommentIds(notReplyCommentIds);
            // 将第一次回复的评论id同步到redis
            taskExecutor.submit(() -> {
               List<Long> needSyncCommentIds = commentDos.stream()
                       .filter(commentDo -> commentDo.getFirstReplyCommentId() != 0)
                       .map(CommentDo::getId)
                       .toList();
               // 批量更新
                SyncCommentRedis(needSyncCommentIds);
            });
            // todo 将第一次回复的评论id为0的更新到数据库
            // 筛选
            List<CommentDo> commentDoList = commentDos.stream()
                    .filter(commentDo -> commentDo.getFirstReplyCommentId() == 0)
                    .toList();
            List<Long> syncIds = new ArrayList<>();
            commentDoList.forEach(commentDo -> {
                // 更新
                Long firstRepliedCommentId = commentDo.getId(); // 被回复的评论id
                // 第一次回复的评论
                CommentDo firstReplyComment = commentDoMapper.selectEarliestByParentId(firstRepliedCommentId);
                log.info("【一级评论首次回复更新】 获取到第一次回复的评论： {}", firstReplyComment);
                if (Objects.nonNull(firstReplyComment)) {
                    // 存在这个评论，则更新数据库
                    Long commentId = firstReplyComment.getId();
                    commentDoMapper.updateFirstReplyCommentIdByPrimaryKey(commentId, firstRepliedCommentId);
                    syncIds.add(firstRepliedCommentId);
                }
            });
            taskExecutor.submit(() -> {
                // 同步到redis
                SyncCommentRedis(syncIds);
            });
        }
    }

    /**
     * 同步评论id到redis
     * @param needSyncCommentIds 需要同步的评论id
     */
    private void SyncCommentRedis(List<Long> needSyncCommentIds) {
        // 获取 ValueOperations
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        // 使用 RedisTemplate 的管道模式，允许在一个操作中批量发送多个命令，防止频繁操作 Redis
        redisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
            needSyncCommentIds.forEach(needSyncCommentId -> {
                // 构建 Redis Key
                String key = CommentContentKeyConstant.getReplyCommentId(needSyncCommentId);

                // 批量设置值并指定过期时间（5小时以内）
                valueOperations.set(key, 1, RandomUtil.randomInt(5 * 60 * 60), TimeUnit.SECONDS);
            });
            return null;
        });
    }
}
