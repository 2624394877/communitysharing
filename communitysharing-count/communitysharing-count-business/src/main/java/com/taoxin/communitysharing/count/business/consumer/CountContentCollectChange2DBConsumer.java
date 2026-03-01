package com.taoxin.communitysharing.count.business.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.domain.mapper.ContentCountDoMapper;
import com.taoxin.communitysharing.count.business.domain.mapper.UserCountDoMapper;
import com.taoxin.communitysharing.count.business.model.dto.AggregationCountCollectUnCollectMQDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE_2_DB,
        topic = MQConstant.TOPIC_CONTENT_COLLECT_COUNT_CHANGE_2_DB
)
public class CountContentCollectChange2DBConsumer implements RocketMQListener<String> {
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private ContentCountDoMapper contentCountDoMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private UserCountDoMapper userCountDoMapper;

    @Override
    public void onMessage(String body) {
        rateLimiter.acquire();
        log.info("【内容收藏数变化入库】消费者接收到消息: {}", body);
        List<AggregationCountCollectUnCollectMQDTO> aggregationCountCollectUnCollectMQDTOS = null;
        try {
            aggregationCountCollectUnCollectMQDTOS = JsonUtil.parseList(body, AggregationCountCollectUnCollectMQDTO.class);
        } catch (Exception e) {
            log.error("【内容点赞数变化入库】消息解析失败: {}", body, e);
            return;
        }

        if (CollUtil.isNotEmpty(aggregationCountCollectUnCollectMQDTOS)) {
            aggregationCountCollectUnCollectMQDTOS.forEach((aggregationCountCollectUnCollectMQDTO) -> {
                Long contentId = aggregationCountCollectUnCollectMQDTO.getContentId();
                Long creatorId = aggregationCountCollectUnCollectMQDTO.getCreatorId();
                Integer collectCount = aggregationCountCollectUnCollectMQDTO.getCollectCount();
                transactionTemplate.execute(status -> {
                   try {
                       contentCountDoMapper.insertOrUpdateCollectTotalByContentId(contentId, collectCount);
                       userCountDoMapper.insertOrUpdateCollectTotalByUserId(collectCount, creatorId);
                       return true;
                   } catch (Exception e) {
                       log.error("【内容收藏数变化入库】数据库更新失败, contentId: {}, count: {}", contentId, collectCount, e);
                       status.setRollbackOnly();
                       return false;
                   }
                });
            });
        }
    }
}
