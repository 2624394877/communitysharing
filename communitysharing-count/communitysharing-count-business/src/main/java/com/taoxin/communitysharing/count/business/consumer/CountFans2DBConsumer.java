package com.taoxin.communitysharing.count.business.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.constant.MQConstant;
import com.taoxin.communitysharing.count.business.domain.mapper.UserCountDoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RocketMQMessageListener(consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_COUNT_FANS_2_DB, // Group 组
        topic = MQConstant.TOPIC_COUNT_FANS_2_DB // 主题 Topic
)
@Slf4j
public class CountFans2DBConsumer implements RocketMQListener<String> {

    // 令牌桶限流
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private UserCountDoMapper userCountDoMapper;
    @Override
    public void onMessage(String body) {
        // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
        rateLimiter.acquire();
        log.info("## 消费到了 MQ 【计数: 粉丝数入库】, {}...", body);
        Map<Long, Integer> countMap = null;
        try { // common的json工具类抛出了异常，这里需要处理
            countMap = JsonUtil.parseMap(body,Long.class,Integer.class);
        } catch (Exception e) {
            log.error("===x==> json 字符串解析错误",e);
        }
        if (CollUtil.isNotEmpty(countMap)) {
            countMap.forEach((k,v)->{
                // 数据库插入判断
                userCountDoMapper.insertOrUpdateFansTotalByUserId(v,k);
            });
        }
    }
}
