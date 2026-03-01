package com.taoxin.communitysharing.content.business.consumer;

import com.taoxin.communitysharing.content.business.constant.ContentDetailsKeyConstant;
import com.taoxin.communitysharing.content.business.constant.MQConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "communitysharing_group"+MQConstant.TOPIC_DELAY_DELETE_CONTENT_LOCAL_CACHE,
        topic = MQConstant.TOPIC_DELAY_DELETE_CONTENT_LOCAL_CACHE // 订阅的Topic
)
public class DeleteContentLocalCacheDelayConsumer implements RocketMQListener<Long> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public void onMessage(Long contentId) {
        log.info("[DeleteContentLocalCacheDelayConsumer] 延时删除内容ID为{}的缓存", contentId);

        String redisDetailKey = ContentDetailsKeyConstant.getContentDetailsKey(contentId);
        redisTemplate.delete(redisDetailKey);
    }
}
