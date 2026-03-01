package com.taoxin.communitysharing.count.business.consumer;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.constant.RedisKeyConstant;
import com.taoxin.communitysharing.count.business.domain.mapper.UserCountDoMapper;
import com.taoxin.communitysharing.count.business.model.dto.ContentOperateMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_CONTENT_OPERATION_RECORD,
        topic = MQConstant.TOPIC_CONTENT_OPERATION_RECORD
)
public class CountContentPublishConsumer implements RocketMQListener<Message> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserCountDoMapper userCountDoMapper;
    @Override
    public void onMessage(Message message){
        String bodyString = new String(message.getBody());
        String Tag = message.getTags();
        log.info("【内容发布/删除计数】 收到消息: tag={}, body={}, messageId={}",Tag, bodyString, message.getBuyerId());
        if (Objects.equals(Tag, MQConstant.TAG_CREATE_CONTENT)){
            // todo 处理发布内容的消息
            HandlePublishContentTagMessage(bodyString,1);
        } else if (Objects.equals(Tag, MQConstant.TAG_DELETE_CONTENT)){
            // todo 处理删除内容的消息
            HandlePublishContentTagMessage(bodyString,-1);
        }
    }

    private void HandlePublishContentTagMessage(String bodyString, long count) {
        ContentOperateMqDTO contentOperateMqDTO = JsonUtil.parseObject(bodyString, ContentOperateMqDTO.class);
        if (Objects.isNull(contentOperateMqDTO)) return;
        Long creatorId = contentOperateMqDTO.getCreatorId();
        // todo 更新用户发布内容数量redis
        String userCountKey = RedisKeyConstant.buildCountUserKey(creatorId);
        boolean exists = redisTemplate.hasKey(userCountKey);
        if (exists) {
            redisTemplate.opsForHash().increment(userCountKey, RedisKeyConstant.CONTENT_COUNT_TOTAL, count);
        }
        userCountDoMapper.insertOrUpdateContentTotalByUserId(count, creatorId);
    }
}
