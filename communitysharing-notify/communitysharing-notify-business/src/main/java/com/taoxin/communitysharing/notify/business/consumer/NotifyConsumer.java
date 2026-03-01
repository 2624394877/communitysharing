package com.taoxin.communitysharing.notify.business.consumer;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.notify.business.constant.MQConstant;
import com.taoxin.communitysharing.notify.business.handler.NotifyWebSocketHandler;
import com.taoxin.communitysharing.notify.business.model.dto.NotifyDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.NOTIFY_TOPIC,
        topic = MQConstant.NOTIFY_TOPIC
)
@Slf4j
public class NotifyConsumer implements RocketMQListener<String> {
    @Resource
    private NotifyWebSocketHandler notifyWebSocketHandler;

    @Override
    public void onMessage(String msg) {
        NotifyDTO dto = JsonUtil.parseObject(msg, NotifyDTO.class);
        try {
            notifyWebSocketHandler.sendToUser(dto);
        } catch (IOException e) {
            log.error("==== 消息发送失败 ====> userId: {}, content: {}", dto.getUserId(), dto.getContent(), e);
        }
    }
}
