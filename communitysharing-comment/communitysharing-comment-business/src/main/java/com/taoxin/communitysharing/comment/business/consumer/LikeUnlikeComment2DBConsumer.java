package com.taoxin.communitysharing.comment.business.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.comment.business.constant.CommentContentKeyConstant;
import com.taoxin.communitysharing.comment.business.constant.MQConstant;
import com.taoxin.communitysharing.comment.business.constant.RedisKeyConstant;
import com.taoxin.communitysharing.comment.business.domain.mapper.CommentLikeDoMapper;
import com.taoxin.communitysharing.comment.business.enums.LikeUnLikeTypeEnum;
import com.taoxin.communitysharing.comment.business.model.dto.LikeUnLikeCommentDTO;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LikeUnlikeComment2DBConsumer {
    @Value("${rocketmq.name-server}")
    private String nameServer;
    @Resource
    private RateLimiter rateLimiter;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;
    @Resource
    private CommentLikeDoMapper commentLikeDoMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private DefaultMQPushConsumer consumer;

    @Bean("LikeUnlikeComment2DBConsumer")
    public DefaultMQPushConsumer mqPushConsumer() throws Exception {
        String GroupName = "communitysharing_group_" + MQConstant.TOPIC_COMMENT_LIKE;
        consumer = new DefaultMQPushConsumer(GroupName);
        consumer.setNamesrvAddr(nameServer);
        consumer.subscribe(MQConstant.TOPIC_COMMENT_LIKE, "*");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setMaxReconsumeTimes(3);
        consumer.setConsumeMessageBatchMaxSize(30); // 每批次消费的最大消息数量，这里设置为 30，表示每次拉取时最多消费 30 条消息
        consumer.setPullInterval(1000);

        consumer.registerMessageListener((MessageListenerConcurrently)(msgs, context) -> {
            log.info("【评论点赞/取消点赞】 收到消息: size={}", msgs.size());
            try {
                rateLimiter.acquire(); // 获取令牌，如果获取不到则阻塞
                List<LikeUnLikeCommentDTO> likeUnlikeCommentDTOs = Lists.newArrayList();
                msgs.forEach(msg -> {
                    String body = new String(msg.getBody());
                    likeUnlikeCommentDTOs.add(JsonUtil.parseObject(body, LikeUnLikeCommentDTO.class));
                });
                log.info("【评论点赞/取消点赞】消息体: {}", likeUnlikeCommentDTOs);
                // 分组
                Map<Long, List<LikeUnLikeCommentDTO>> commentOperations = likeUnlikeCommentDTOs.stream()
                        .collect(Collectors.groupingBy(LikeUnLikeCommentDTO::getCommentId));
                List<LikeUnLikeCommentDTO> likeUnLikeCommentDTOS = Lists.newArrayList();
                commentOperations.forEach((commentId, operations) -> {
                    log.info("【评论点赞/取消点赞】开始处理评论点赞/取消点赞: commentId={}", commentId);
                    // 第二次分组
                    Map<Long, LikeUnLikeCommentDTO> userLastOperations = operations.stream()
                            // 获取最后一次操作
                            .collect(Collectors.toMap(LikeUnLikeCommentDTO::getUserId, Function.identity(), (old, newOne) -> {
                                return old.getCreateTime().isAfter(newOne.getCreateTime()) ? old : newOne;
                            }));
                    likeUnLikeCommentDTOS.addAll(userLastOperations.values());
                });

                executeBatchSql(likeUnLikeCommentDTOS);

//                asyncSetbloomCommentLikes(likeUnlikeCommentDTOs);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                log.error("【评论点赞/取消点赞】消息解析失败: {}", e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        // todo

        consumer.start();
        return consumer;
    }

    private void asyncSetbloomCommentLikes(List<LikeUnLikeCommentDTO> likeUnlikeCommentDTOs) {
        taskExecutor.submit(() -> {
            try {
                // 点赞的过滤出来
                List<LikeUnLikeCommentDTO> likes = likeUnlikeCommentDTOs.stream()
                        .filter(dto -> Objects.equals(dto.getType(), LikeUnLikeTypeEnum.LIKE.getCode()))
                        .toList();
                // 以评论id进行分组
                Map<String, List<Long>> commentIds =
                        likes.stream()
                                .collect(Collectors.groupingBy(
                                        dto -> "comment:like:" + dto.getCommentId(),
                                        Collectors.mapping(LikeUnLikeCommentDTO::getUserId, Collectors.toList())
                                ));
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/batch_init_bloom_like_unlike_comment.lua")));
                redisScript.setResultType(Long.class);
                commentIds.forEach((key, value) -> {
                    long expireTime = 60 * 60 * 12;

                    List<Object> args = Lists.newArrayList();
                    args.add(expireTime);
                    args.addAll(value);

                    redisTemplate.execute(
                            redisScript,
                            Collections.singletonList(key),
                            args.toArray()
                    );
                });
             } catch (Exception e) {
                log.error("========= 【点赞消息】 异步更新过滤器失败：{}", e);
            }
        });
    }

    private void executeBatchSql(List<LikeUnLikeCommentDTO> likeUnLikeCommentDTOS) {
        List<LikeUnLikeCommentDTO> like = Lists.newArrayList();
        List<LikeUnLikeCommentDTO> unlike = Lists.newArrayList();

        for (LikeUnLikeCommentDTO dto : likeUnLikeCommentDTOS) {
            if (Objects.equals(dto.getType(), LikeUnLikeTypeEnum.LIKE.getCode())) {
                like.add(dto);
            } else {
                unlike.add(dto);
            }
        }
        if (!like.isEmpty()) {
            commentLikeDoMapper.batchInsert(like);
        }
        if (!unlike.isEmpty()) {
            commentLikeDoMapper.batchDelete(unlike);
        }
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(consumer)) {
            try {
                consumer.shutdown();
            } catch (Exception e) {
                log.error("【评论点赞/取消点赞】消费者关闭失败", e);
            }
        }
    }
}
