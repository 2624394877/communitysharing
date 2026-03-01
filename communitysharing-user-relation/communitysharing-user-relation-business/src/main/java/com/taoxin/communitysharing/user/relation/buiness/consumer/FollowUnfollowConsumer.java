package com.taoxin.communitysharing.user.relation.buiness.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.taoxin.communitysharing.common.uitl.DateUtil;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.user.relation.buiness.constant.MQConstant;
import com.taoxin.communitysharing.user.relation.buiness.constant.RedisKeyConstant;
import com.taoxin.communitysharing.user.relation.buiness.domain.databaseObject.FansDo;
import com.taoxin.communitysharing.user.relation.buiness.domain.databaseObject.FollowingDo;
import com.taoxin.communitysharing.user.relation.buiness.domain.mapper.FansDoMapper;
import com.taoxin.communitysharing.user.relation.buiness.domain.mapper.FollowingDoMapper;
import com.taoxin.communitysharing.user.relation.buiness.enums.FollowUnfollowTypeEnum;
import com.taoxin.communitysharing.user.relation.buiness.model.dto.CountFollowUnfollowMqDTO;
import com.taoxin.communitysharing.user.relation.buiness.model.dto.FollowUserMqDTO;
import com.taoxin.communitysharing.user.relation.buiness.model.dto.UnfollowUserMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "communitysharing_group" + MQConstant.FOLLOW_UNFOLLOW_TOPIC,
        topic = MQConstant.FOLLOW_UNFOLLOW_TOPIC, // 订阅的Topic
        consumeMode = ConsumeMode.ORDERLY // 顺序消费
)
public class FollowUnfollowConsumer implements RocketMQListener<Message> {
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private FollowingDoMapper followingDoMapper;
    @Resource
    private FansDoMapper fansDoMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    // 令牌桶限流
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Message message) {
        rateLimiter.acquire(); // 获取令牌
        String bodyStr = new String(message.getBody());

        String tag = message.getTags();
        log.info("========= tag: {}, body: {} =============", tag, bodyStr);
        if (Objects.equals(tag, MQConstant.FOLLOW_TAG)) {
            // 关注
            handlerFollowTagMessage(bodyStr);
        }else if (Objects.equals(tag, MQConstant.UN_FOLLOW_TAG)) {
            // 取消关注
            handlerUnFollowTagMessage(bodyStr);
        }
    }

    private void handlerUnFollowTagMessage(String bodyStr) {
        // 解析消息
        UnfollowUserMqDTO unfollowUserMqDTO = JsonUtil.parseObject(bodyStr, UnfollowUserMqDTO.class);
        if (Objects.isNull(unfollowUserMqDTO)) return;
        Long userId = unfollowUserMqDTO.getUserId();
        Long unfollowingUserId = unfollowUserMqDTO.getUnfollowUserId();
        LocalDateTime createTime = unfollowUserMqDTO.getCreateTime();
        // 删除数据 使用编程式事务
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            log.info("========= 删除数据, 删除用户: {}, 被删除用户: {} ============", userId, unfollowingUserId);
            try {
                int count = followingDoMapper.deleteByUserIdAndFolllowingUserId(userId, unfollowingUserId);
                if (count > 0) {
                    fansDoMapper.deleteByUserIdAndFansUserId(unfollowingUserId, userId);
                }
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("========= 删除数据库失败 ============", e);
                return false;
            }
            return true;
        }));
        log.info("========= 删除数据库结果: {} ============", isSuccess);
        if (isSuccess) {
            // 删除redis数据粉丝
            String fansRedisKey = RedisKeyConstant.getUserFansRelationKey(unfollowingUserId);
            redisTemplate.opsForZSet().remove(fansRedisKey, userId); // 删除Zset集合中数据

            // 发送消息 给计数服务 统计粉丝数&关注数
            CountFollowUnfollowMqDTO countFollowUnfollowMqDTO = CountFollowUnfollowMqDTO.builder()
                    .userId(userId)
                    .targetUserId(unfollowingUserId)
                    .type(FollowUnfollowTypeEnum.UNFOLLOW.getCode())
                    .build();
            sendMQ(countFollowUnfollowMqDTO);
        } // 删除关注表和粉丝表数据成功，则发送消息 给计数服务 统计粉丝数&关注数
    }

    public void handlerFollowTagMessage(String bodyStr) {
        // 解析消息
        FollowUserMqDTO followUserMqDTO = JsonUtil.parseObject(bodyStr, FollowUserMqDTO.class);
        if (Objects.isNull(followUserMqDTO)) return; // 解析失败
        Long userId = followUserMqDTO.getUserId();
        Long followingUserId = followUserMqDTO.getFollowingUserId();
        LocalDateTime createTime = followUserMqDTO.getCreateTime();
        // 插入数据库 使用编程式事务
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            FollowingDo followingDo = null;
            try {
                followingDo = FollowingDo.builder()
                        .userId(userId)
                        .followingUserId(followingUserId)
                        .createTime(createTime)
                        .build();
                int count = followingDoMapper.insertSelective(followingDo);
                if (count > 0) {
                    fansDoMapper.insertSelective(FansDo.builder()
                            .userId(followingUserId)
                            .fansUserId(userId)
                            .createTime(createTime)
                            .build());
                } // 关注表插入成功，则插入粉丝表
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("========= 插入数据库失败 ============", e);
            }
            return false;
        }));
        log.info("========= 插入数据库结果: {} ============", isSuccess);
        if (isSuccess) {
            // 数据库插入成功，将数据放入到redis中
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_update_fans_zset.lua")));
            script.setResultType(Long.class);
            long timestamp = DateUtil.LocalTimestampToDate(createTime); // 时间戳
            String fansRediskey = RedisKeyConstant.getUserFansRelationKey(followingUserId);
            log.info("========= 插入redis数据, 粉丝rediskey: {}, 粉丝id: {}, 时间戳: {} ============", fansRediskey, userId, timestamp);
            Long result = redisTemplate.execute(script, Collections.singletonList(fansRediskey), userId, timestamp);
            log.info("========= 插入redis结果: {} ============", result);

            // 发送消息 给计数服务 统计粉丝数&关注数
            CountFollowUnfollowMqDTO countFollowUnfollowMqDTO = CountFollowUnfollowMqDTO.builder()
                    .userId(userId)
                    .targetUserId(followingUserId)
                    .type(FollowUnfollowTypeEnum.FOLLOW.getCode())
                    .build();
            sendMQ(countFollowUnfollowMqDTO);
        } // 数据库插入成功，将数据放入到redis中，发送消息 给计数服务 统计粉丝数&关注数
    }

    private void sendMQ(CountFollowUnfollowMqDTO countFollowUnfollowMqDTO) {
        org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(countFollowUnfollowMqDTO))
                .build();
        rocketMQTemplate.asyncSend(MQConstant.FOLLOW_COUNT, message, new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========= 【计数服务(关注数)】 ====> {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("========= 【计数服务(关注数)】 发送失败 ============", throwable);
            }
        });

        rocketMQTemplate.asyncSend(MQConstant.FANS_COUNT, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========= 【计数服务(粉丝数)】====> {}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("========= 【计数服务(粉丝数)】发送失败 ============", throwable);
            }
        });
    }
}
