package com.taoxin.communitysharing.comment.business.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.taoxin.communitysharing.comment.business.constant.CountConentRedisKeyConstant;
import com.taoxin.communitysharing.comment.business.constant.MQConstant;
import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentDo;
import com.taoxin.communitysharing.comment.business.domain.mapper.CommentDoMapper;
import com.taoxin.communitysharing.comment.business.domain.mapper.ContentCountDoMapper;
import com.taoxin.communitysharing.comment.business.enums.CommentLevelEnum;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
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
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_COMMENT_DELETE_COUNT,
        topic = MQConstant.TOPIC_COMMENT_DELETE_COUNT
)
public class DeleteCommentConsumer implements RocketMQListener<String> {
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private CommentDoMapper commentDoMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ContentCountDoMapper contentCountDoMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(String s) {
        rateLimiter.acquire();

        CommentDo commentDo = JsonUtil.parseObject(s, CommentDo.class);

        CommentLevelEnum commentLevelEnum = null;
        if (Objects.nonNull(commentDo)) {
            Integer level = commentDo.getLevel();
            commentLevelEnum = CommentLevelEnum.getByCode(level);
        }
        switch (Objects.requireNonNull(commentLevelEnum)) {
            case FIRST_LEVEL -> HandleLeve1Comment(commentDo);
            case SECOND_LEVEL -> HandleLeve2Comment(commentDo);
        }
    }

    private void HandleLeve1Comment(CommentDo commentDo) {
        // todo 找到评论的关系链，并删除
        Long commentId = commentDo.getId();
        Long contentId = commentDo.getContentId();
        int count = commentDoMapper.DeleteByCommentId(commentId);
        // todo 计数更新
        String ContentKey = CountConentRedisKeyConstant.getCountContentKeyPrefix(contentId);
        boolean isEXIST = redisTemplate.hasKey(ContentKey);
        if (isEXIST) {
            redisTemplate.opsForHash().increment(ContentKey, CountConentRedisKeyConstant.COMMENT_TOTAL, -(count+1));
        }
        contentCountDoMapper.UpdateCommentCountByContentId(contentId, -(count+1));
    }

    private void HandleLeve2Comment(CommentDo commentDo) {
        Long commentId = commentDo.getId();
        // todo 找到评论的关系链，并删除
        int count = commentDoMapper.DeleteCommentByReplyCommentId(commentId);
        // todo 计数更新（一级）
        Long parentId = commentDo.getParentId();
        String commentIdKey = CountConentRedisKeyConstant.getCountCommentKeyPrefix(parentId);
        boolean isEXIST = redisTemplate.hasKey(commentIdKey);
        if (isEXIST) {
            redisTemplate.opsForHash().increment(commentIdKey, CountConentRedisKeyConstant.CHILD_COMMENT_TOTAL, -(count+1));
        }
        // todo 如果是第一条评论，则更新字段
        CommentDo parentCommentDo = commentDoMapper.selectByPrimaryKey(parentId);
        Long firstReplyCommentId = Objects.nonNull(parentCommentDo)? parentCommentDo.getFirstReplyCommentId() : 0;
        if (Objects.equals(commentId, firstReplyCommentId)) {
            CommentDo lestCommentDo = commentDoMapper.selectEarliestByParentId(parentId);
            Long lestCommentId = lestCommentDo.getId();
            commentDoMapper.updateFirstReplyCommentIdByPrimaryKey(lestCommentId, parentId);
        }
        // todo 计数更新（二级）
        // todo 计算热度
        Set<Long> commentIds = Sets.newHashSetWithExpectedSize(1); // 创建不会扩容的HashSet
        commentIds.add(commentId);
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(commentIds)).build();
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_COMMENT_UPDATE_HEAT, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========= 【计数服务(二级评论数)】 ====> {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("========= 【计数服务(二级评论数)】 ====> {}", throwable);
            }
        });
    }

    private void recurrentGetReplyCommentId(Long commentId, List<Long> replyCommentIds) {
        List<CommentDo> commentDos = commentDoMapper.selectByReplyCommentId(commentId);
        if (Objects.nonNull(commentDos) && CollUtil.isNotEmpty(commentDos)) {
            for (CommentDo commentDo : commentDos) {
                replyCommentIds.add(commentDo.getId());
            }
        }
    }
}
