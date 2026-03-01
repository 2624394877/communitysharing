package com.taoxin.communitysharing.content.business.consumer;

import com.taoxin.communitysharing.content.business.constant.MQConstant;
import com.taoxin.communitysharing.content.business.service.ContentServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "communitysharing_group",
        topic = MQConstant.TOPIC_DELETE_CONTENT_LOCAL_CACHE, // 订阅的Topic
        messageModel = MessageModel.BROADCASTING // 广播模式
)
public class DeleteContentLocalCacheConsumer implements RocketMQListener<Long> {
    @Resource
    private ContentServer  contentServer;
    @Override
    public void onMessage(Long contentId) {
        log.info("[DeleteContentLocalCacheConsumer] 删除内容ID为{}的缓存", contentId);
        contentServer.DeleteContentLocalCache(contentId);
    }
}
