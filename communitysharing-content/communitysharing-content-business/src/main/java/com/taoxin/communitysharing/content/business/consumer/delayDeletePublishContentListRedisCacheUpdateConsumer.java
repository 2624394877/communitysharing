package com.taoxin.communitysharing.content.business.consumer;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.content.business.constant.ContentDetailsKeyConstant;
import com.taoxin.communitysharing.content.business.constant.MQConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_DELAY_DELETE_PUBLISHED_CONTENT_LIST_REDIS_CACHE_UPDATE,
        topic = MQConstant.TOPIC_DELAY_DELETE_PUBLISHED_CONTENT_LIST_REDIS_CACHE_UPDATE
)
public class delayDeletePublishContentListRedisCacheUpdateConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String,Object> redis;

    @Override
    public void onMessage(String body) {
        log.info("[delayDeletePublishContentListRedisCacheUpdateConsumer] 收到消息数据：{}", body);
        try {
            List<Long> Ids = JsonUtil.parseList(body, Long.class);
            Long userId = Ids.get(0);
            Long contentId = Ids.get(1);

            String userContentPublishedListKey = ContentDetailsKeyConstant.getUserPublishContentListKey(userId);
            String contentDetailsKey = ContentDetailsKeyConstant.getContentDetailsKey(contentId);
            redis.delete(Arrays.asList(userContentPublishedListKey, contentDetailsKey));
        } catch (Exception e) {
            log.error("[delayDeletePublishContentListRedisCacheUpdateConsumer] 解析消息数据失败", e);
        }

    }
}
