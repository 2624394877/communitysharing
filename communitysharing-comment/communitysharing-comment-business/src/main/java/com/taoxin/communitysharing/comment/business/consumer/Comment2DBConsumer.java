package com.taoxin.communitysharing.comment.business.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.comment.business.constant.MQConstant;
import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentDo;
import com.taoxin.communitysharing.comment.business.domain.mapper.CommentDoMapper;
import com.taoxin.communitysharing.comment.business.enums.CommentLevelEnum;
import com.taoxin.communitysharing.comment.business.model.bo.CommentBo;
import com.taoxin.communitysharing.comment.business.model.dto.CountPublishCommentMqDTO;
import com.taoxin.communitysharing.comment.business.model.dto.PublishCommentMqDTO;
import com.taoxin.communitysharing.comment.business.rpc.KVFeignApiService;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Comment2DBConsumer {

    @Value("${rocketmq.name-server}")
    private String nameServer;
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private CommentDoMapper commentDoMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private KVFeignApiService kvFeignApiService;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private DefaultMQPushConsumer consumer;

    @Bean
    public DefaultMQPushConsumer pushConsumer() throws MQClientException {
        String GroupName = "communitysharing_group_" + MQConstant.TOPIC_PUBLISH_COMMENT;

        // 创建一个新的 DefaultMQPushConsumer 实例，并指定消费者的消费组名
        consumer = new DefaultMQPushConsumer(GroupName);
        // 设置 NameServer 的地址
        consumer.setNamesrvAddr(nameServer);
        // 订阅指定的主题，并设置主题的订阅规则（"*" 表示订阅所有标签的消息）
        consumer.subscribe(MQConstant.TOPIC_PUBLISH_COMMENT, "*");
        // 设置消费者消费消息的起始位置，如果队列中没有消息，则从最新的消息开始消费。
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        // 设置消息消费模式，这里使用集群模式 (CLUSTERING)
        consumer.setMessageModel(MessageModel.CLUSTERING);
        // 设置每批次消费的最大消息数量，这里设置为 30，表示每次拉取时最多消费 30 条消息
        consumer.setConsumeMessageBatchMaxSize(30);
        // 注册消息监听器
        consumer.registerMessageListener((MessageListenerConcurrently)(msgs, context)->{
            log.info("【评论数据对齐】开始消费：{}", msgs.size());
            try {
                rateLimiter.acquire();
                // 消息体 Json 字符串转 DTO
                List<PublishCommentMqDTO> publishCommentMqDTOS = Lists.newArrayList();
                msgs.forEach(msg -> {
                    String body = new String(msg.getBody());
                    log.info("【评论数据对齐】消费消息：{}", body);
                    publishCommentMqDTOS.add(JsonUtil.parseObject(body, PublishCommentMqDTO.class));
                });
                // 提取所有不为空的回复评论 ID
                List<Long> replayCommentIds = publishCommentMqDTOS.stream()
                        .filter(publishCommentMqDTO -> Objects.nonNull(publishCommentMqDTO.getReplayCommentId()))
                        .map(PublishCommentMqDTO::getReplayCommentId).toList();
                // 批量查询相关回复评论记录
                List<CommentDo> commentDOS = null;
                if (CollUtil.isNotEmpty(replayCommentIds)) {
                    commentDOS = commentDoMapper.selectByCommentIds(replayCommentIds);
                }
                // DO 集合转 <评论 ID - 评论 DO> 字典, 以方便后续查找
                Map<Long, CommentDo> commentDoMap = Maps.newHashMap();
                if (CollUtil.isNotEmpty(commentDOS)) {
                    commentDoMap = commentDOS.stream().collect(Collectors.toMap(CommentDo::getId, commentDo-> commentDo));
                }
                // todo 构建评论 BO
                List<CommentBo> commentBOS = Lists.newArrayList();
                for (PublishCommentMqDTO publishCommentMqDTO : publishCommentMqDTOS) {
                    String imageUrl = publishCommentMqDTO.getImageUrl();
                    CommentBo commentBo = CommentBo.builder()
                            .id(publishCommentMqDTO.getCommentId())
                            .contentId(publishCommentMqDTO.getContentId())
                            .userId(publishCommentMqDTO.getCreatorId())
                            .isContentEmpty(true) // 评论内容 默认为空
                            .imageUrl(Objects.isNull(imageUrl) ? "" : imageUrl)
                            .level(CommentLevelEnum.FIRST_LEVEL.getCode()) // 评论级别默认1
                            .parentId(publishCommentMqDTO.getContentId()) // 父级评论 ID默认为内容id
                            .createTime(publishCommentMqDTO.getCreateTime())
                            .updateTime(publishCommentMqDTO.getCreateTime())
                            .isTop(false) // 是否置顶
                            .replyTotal(0L) // 回复总数
                            .likeTotal(0L) // 点赞总数
                            .replyCommentId(0L) // 回复的评论 ID
                            .replyUserId(0L) // 回复的用户 ID
                            .build();
                    String content  = publishCommentMqDTO.getContent();
                    if(StringUtils.isNotEmpty(content)) {
                        commentBo.setContentUuid(UUID.randomUUID().toString());
                        commentBo.setIsContentEmpty(false);
                        commentBo.setContent(content);
                    }

                    // 设置评论级别、回复用户 ID (reply_user_id)、父评论 ID (parent_id)
                    Long replyCommentId = publishCommentMqDTO.getReplayCommentId();
                    if (Objects.nonNull(replyCommentId)) {
                        CommentDo commentDo = commentDoMap.get(replyCommentId);
                        if (Objects.nonNull(commentDo)) {
                            // 若回复的评论 ID 不为空，说明是二级评论
                            commentBo.setLevel(CommentLevelEnum.SECOND_LEVEL.getCode());
                            commentBo.setReplyUserId(publishCommentMqDTO.getReplayCommentId());
                            // 父评论 ID
                            commentBo.setParentId(commentDo.getId());
                            if (Objects.equals(commentDo.getLevel(), CommentLevelEnum.SECOND_LEVEL.getCode())) {
                                // 如果回复的评论为二级评论，则父级评论 ID为回复的评论的父级评论 ID
                                commentBo.setParentId(commentDo.getParentId());
                            }
                            // 回复的哪个用户
                            commentBo.setReplyUserId(commentDo.getUserId());
                        }
                    }
                    commentBOS.add(commentBo);
                }
                log.info("【评论数据对齐】开始插入：{}", JsonUtil.toJsonString(commentBOS));

                // todo 批量插入 编程式事务异步写入
                Integer result = transactionTemplate.execute(status -> {
                    try {
                        // 先批量存入评论元数据
                        int count = commentDoMapper.batchInsert(commentBOS);

                        // 过滤出评论内容不为空的 BO
                        List<CommentBo> commentContentBOS = commentBOS.stream().filter(commentBo -> Boolean.FALSE.equals(commentBo.getIsContentEmpty())).toList();
                        if (CollUtil.isNotEmpty(commentContentBOS)) {
                            // 批量插入评论内容
                            kvFeignApiService.batchSaveCommentContent(commentContentBOS);
                        }
                        return count;
                    } catch (Exception e) {
                        log.error("【评论数据对齐】插入数据库失败：{}", e);
                        status.setRollbackOnly();
                        throw e;
                    }
                });
                if (Objects.nonNull(result) && result > 0) {
                    // 构建计数消息
                    List<CountPublishCommentMqDTO> countPublishCommentMqDTOS = commentBOS.stream()
                            .map(commentBo -> CountPublishCommentMqDTO.builder()
                                    .contentId(commentBo.getContentId())
                                    .commentId(commentBo.getId())
                                    .level(commentBo.getLevel())
                                    .parentId(commentBo.getParentId())
                                    .build()
                            )
                            .toList();
                    Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(countPublishCommentMqDTOS)).build();
                    rocketMQTemplate.asyncSend(MQConstant.TOPIC_COUNT_COMMENT, message, new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            log.info("========= 【计数服务(评论数)】 ====> {}", sendResult);
                        }

                        @Override
                        public void onException(Throwable throwable) {
                            log.error("【计数服务(评论数)】发送失败：{}", throwable);
                        }
                    });
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS; // 消费成功
            } catch (Exception e) {
                log.error("【评论数据对齐】消费失败：{}", e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER; // 稍后再试
            }
        });
        consumer.start();
        return consumer;
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(consumer)) {
            try {
                consumer.shutdown();
            } catch (Exception e) {
                log.error("【评论数据对齐】消费者关闭失败", e);
            }
        }
    }

}
