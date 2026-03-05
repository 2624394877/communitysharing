package com.taoxin.communitysharing.user.business.consumer;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import com.taoxin.communitysharing.user.business.config.GlobalCaffeine;
import com.taoxin.communitysharing.user.business.constant.MQConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group" + MQConstant.USER_TOPIC_CACHE,
        topic = MQConstant.USER_TOPIC_CACHE,
        messageModel = MessageModel.BROADCASTING // 广播模式
)
public class UserUpdateLocalConsumer implements RocketMQListener<String> {
    @Resource
    private GlobalCaffeine globalCaffeine;
    @Override
    public void onMessage(String body) {
        log.info("用户更新本地缓存：{}", body);
        FindUserByIdResDTO findUserByIdResDTO = JsonUtil.parseObject(body, FindUserByIdResDTO.class);
        if (Objects.isNull(findUserByIdResDTO)) return;
        Long userId = findUserByIdResDTO.getId();
        findUserByIdResDTO.setId(null);
        globalCaffeine.caffeineCache().put(userId, findUserByIdResDTO);
    }
}
