package com.taoxin.communitysharing.count.business.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.domain.mapper.ContentCountDoMapper;
import com.taoxin.communitysharing.count.business.domain.mapper.UserCountDoMapper;
import com.taoxin.communitysharing.count.business.model.dto.AggregationCountLikeUnlikeMQDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_CONTENT_LIKE_COUNT_CHANGE_2_DB,
        topic = MQConstant.TOPIC_CONTENT_LIKE_COUNT_CHANGE_2_DB
)
public class CountContentLikeChange2DBConsumer implements RocketMQListener<String> {
    @Resource
    private ContentCountDoMapper contentCountDoMapper;
    @Resource
    private RateLimiter rateLimiter; // 注入限流器
    @Autowired
    private UserCountDoMapper userCountDoMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public void onMessage(String body) {
        rateLimiter.acquire(); // 获取一个令牌，若没有则等待
        log.info("【内容点赞数变化入库】消费者接收到消息: {}", body);
        List<AggregationCountLikeUnlikeMQDTO> contentLikeCountChangeMap = null;
        try {
            contentLikeCountChangeMap = JsonUtil.parseList(body, AggregationCountLikeUnlikeMQDTO.class);
        } catch (Exception e) {
            log.error("【内容点赞数变化入库】消息解析失败: {}", body, e);
            return;
        }

        if (CollUtil.isNotEmpty(contentLikeCountChangeMap)) {
            contentLikeCountChangeMap.forEach((aggregationCountLikeUnlikeMQDTO) -> {
                Long creatorId = aggregationCountLikeUnlikeMQDTO.getCreatorId();
                Long contentId = aggregationCountLikeUnlikeMQDTO.getContentId();
                Integer likeCount = aggregationCountLikeUnlikeMQDTO.getLikeCount();
                transactionTemplate.execute(status -> {
                    try {
                        contentCountDoMapper.insertOrUpdateLikeTotalByContentId(contentId, likeCount);
                        userCountDoMapper.insertOrUpdateLikeTotalByUserId(likeCount, creatorId);
                        return true;
                    } catch (Exception e) {
                        log.error("【内容点赞数变化入库】数据库更新失败, contentId: {}, count: {}", contentId, likeCount, e);
                        status.setRollbackOnly(); // 回滚事务
                        return false;
                    }
                });
            });
        }
    }
}
