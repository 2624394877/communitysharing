package com.taoxin.communitysharing.count.business.consumer;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.constant.RedisKeyConstant;
import com.taoxin.communitysharing.count.business.enums.FollowUnfollowTypeEnum;
import com.taoxin.communitysharing.count.business.model.dto.CountFollowUnfollowMqDTO;
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

import java.util.Objects;

@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.FOLLOW_COUNT,
        topic = MQConstant.FOLLOW_COUNT
)
@Slf4j
public class CountFollowConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(String body) {
        log.info("==== 开始消费关注计数消息 ====> {}",body);

        if (StringUtils.isBlank(body)) return;
        // 关注数和粉丝数计数场景不同，单个用户无法短时间内关注大量用户，所以无需聚合
        CountFollowUnfollowMqDTO countFollowUnfollowMqDTO = JsonUtil.parseObject(body, CountFollowUnfollowMqDTO.class);

        // 操作类型：关注 or 取关
        Integer type = countFollowUnfollowMqDTO.getType();
        // 原用户ID
        Long userId = countFollowUnfollowMqDTO.getUserId();

        // 更新 Redis
        String redisKey = RedisKeyConstant.buildCountUserKey(userId);
        // 判断 Hash 是否存在
        boolean isExisted = redisTemplate.hasKey(redisKey);
        // 若存在
        if (isExisted) {
            // 关注数：关注 +1， 取关 -1
            long count = Objects.equals(type, FollowUnfollowTypeEnum.follow.getCode()) ? 1 : -1;
            // 对 Hash 中的 followingTotal 字段进行加减操作
            redisTemplate.opsForHash().increment(redisKey, RedisKeyConstant.FIELD_FOLLOWING_TOTAL, count);
        }

        // 发送 MQ, 关注数写库
        Message<String> message = MessageBuilder.withPayload(body)
                .build();

        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_COUNT_FOLLOWING_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：关注数入库】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：关注数入库】MQ 发送异常: ", throwable);
            }
        });
    }
}
