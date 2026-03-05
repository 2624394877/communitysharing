package com.taoxin.communitysharing.user.business.consumer;

import com.taoxin.communitysharing.user.business.constant.MQConstant;
import com.taoxin.communitysharing.user.business.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group" + MQConstant.USER_TOPIC,
        topic = MQConstant.USER_TOPIC
)
public class UserUpdateConsumer implements RocketMQListener<Long> {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Override
    public void onMessage(Long userId) {
        log.info("[UserUpdateConsumer] 删除用户ID为{}的缓存", userId);
        String userRedisKey = RedisKeyConstants.getUserInfoKey(userId);
        String userInfoRedisKey = RedisKeyConstants.getUserInfoMainKey(userId);
        boolean isInfoExist = redisTemplate.hasKey(userRedisKey);
        boolean isInfoMainExist = redisTemplate.hasKey(userInfoRedisKey);
        if (isInfoExist) redisTemplate.delete(userRedisKey);
        if (isInfoMainExist) redisTemplate.delete(userInfoRedisKey);
    }
}
