package com.taoxin.communitysharing.content.business.consumer;

import com.taoxin.communitysharing.content.business.constant.ContentDetailsKeyConstant;
import com.taoxin.communitysharing.content.business.constant.MQConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_DELAY_DELETE_PUBLISHED_CONTENT_LIST_REDIS_CACHE,
        topic = MQConstant.TOPIC_DELAY_DELETE_PUBLISHED_CONTENT_LIST_REDIS_CACHE
)
public class delayDeletePublishContentListRedisCacheConsumer implements RocketMQListener<Long> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public void onMessage(Long userId) {
        log.info("[delayDeletePublishContentListRedisCacheConsumer] 删除用户ID为{}的缓存", userId);
        // todo判断用户类型 大用户和普通用户 大用户更新，普通用户删除缓存

        if (Objects.nonNull(userId)) {
            String userHomePageCacheKey = ContentDetailsKeyConstant.getUserPublishContentListKey(userId);

            redisTemplate.delete(userHomePageCacheKey);
        }
    }
}
