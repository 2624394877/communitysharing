package com.taoxin.communitysharing.count.business.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.domain.mapper.UserCountDoMapper;
import com.taoxin.communitysharing.count.business.enums.FollowUnfollowTypeEnum;
import com.taoxin.communitysharing.count.business.model.dto.CountFollowUnfollowMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RocketMQMessageListener(consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_COUNT_FOLLOWING_2_DB, // Group 组
        topic = MQConstant.TOPIC_COUNT_FOLLOWING_2_DB // 主题 Topic
)
@Slf4j
public class CountFollowing2DBConsumer implements RocketMQListener<String> {

    @Resource
    private UserCountDoMapper userCountDoMapper;
    @Resource
    private RateLimiter rateLimiter;

    @Override
    public void onMessage(String body) {
        rateLimiter.acquire();
        log.info("## 消费到了 MQ 【计数: 关注数入库】, {}...", body);
        CountFollowUnfollowMqDTO countFollowUnfollowMqDTO = JsonUtil.parseObject(body, CountFollowUnfollowMqDTO.class);
        // 操作类型：关注 or 取关
        Integer type = countFollowUnfollowMqDTO.getType();
        // 原用户ID
        Long userId = countFollowUnfollowMqDTO.getUserId();
        // 关注数：关注 +1， 取关 -1
        int count = Objects.equals(type, FollowUnfollowTypeEnum.follow.getCode()) ? 1 : -1;
        // 判断数据库中，若原用户的记录不存在，则插入；若记录已存在，则直接更新
        userCountDoMapper.insertOrUpdateFollowingTotalByUserId(count, userId);
    }
}
