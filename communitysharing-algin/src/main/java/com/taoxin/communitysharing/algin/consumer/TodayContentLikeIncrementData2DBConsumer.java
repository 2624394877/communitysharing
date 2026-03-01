package com.taoxin.communitysharing.algin.consumer;

import com.alibaba.fastjson.JSON;
import com.taoxin.communitysharing.algin.constant.MQConstant;
import com.taoxin.communitysharing.algin.constant.RedisKeysConstant;
import com.taoxin.communitysharing.algin.constant.TableConstant;
import com.taoxin.communitysharing.algin.domain.mapper.InsertMapper;
import com.taoxin.communitysharing.algin.job.config.TableShardConfig;
import com.taoxin.communitysharing.algin.model.dto.ContentLikeUnLikeMQDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_algin_" + MQConstant.TOPIC_COUNT_CONTENT_LIKE,
        topic = MQConstant.TOPIC_COUNT_CONTENT_LIKE
)
public class TodayContentLikeIncrementData2DBConsumer implements RocketMQListener<String> {
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private InsertMapper insertMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private TableShardConfig tableShardConfig;

    @Override
    public void onMessage(String body) {
        log.info("【数据对齐服务】消费到计数消息： {}", body);
        ContentLikeUnLikeMQDTO contentLikeUnLikeMQDTO = JSON.parseObject(body, ContentLikeUnLikeMQDTO.class);
        if (Objects.isNull(contentLikeUnLikeMQDTO)) return;
        Long contentId = contentLikeUnLikeMQDTO.getContentId();
        Long creatorId = contentLikeUnLikeMQDTO.getCreatorId();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_today_content_like_check.lua")));
        script.setResultType(Long.class);

        String redisKey = RedisKeysConstant.getBloomTodayContentLikesContentId(date);
        Long result = redisTemplate.execute(script, Collections.singletonList(redisKey),contentId);
        RedisScript<Long> bloomAddScript = RedisScript.of("return redis.call('BF.ADD', KEYS[1], ARGV[1])", Long.class);
        log.info("【点赞数据对齐】 {}", result);
        if (Objects.equals(result,0L)) {
            // todo 对应笔记不在过滤器中，将数据插入数据库
            // 根据分片总数，分别获取对应的分片序号
            Long contentIdHashKey = contentId % tableShardConfig.getTableShards();

            // 插入数据库，需要使用回滚
            try {
                // 将日增量变更数据，分别写入两张表
                insertMapper.insertDataAlignContentLikeCountTempTable(TableConstant.buildTableNameSuffix(date,contentIdHashKey),contentId);
                log.info("【点赞数据对齐】对齐数据入库成功");
            } catch (Exception e) {
                log.error("【点赞数据对齐】对齐数据入库失败",e);
            }
            // TODO: 数据库写入成功后，再添加布隆过滤器中
            redisTemplate.execute(bloomAddScript, Collections.singletonList(redisKey),contentId);
        } // 添加布隆过滤器中[笔记的点赞数变更记录]

        String UserBloomKey = RedisKeysConstant.getBloomTodayContentLikesCreatorId(date);
        result = redisTemplate.execute(script, Collections.singletonList(UserBloomKey),creatorId);
        log.info("【用户获得点赞数据对齐】 {}", result);
        if (Objects.equals(result,0L)) {
            try {
                Long userIdHashKey = creatorId % tableShardConfig.getTableShards();
                insertMapper.insertDataAlignUserLikeCountTempTable(TableConstant.buildTableNameSuffix(date,userIdHashKey),creatorId);
            } catch (Exception e) {
                log.error("【用户获得点赞数据对齐】对齐数据入库失败",e);
            }
            // TODO: 数据库写入成功后，再添加布隆过滤器中
            redisTemplate.execute(bloomAddScript, Collections.singletonList(UserBloomKey),creatorId);
        } // 添加布隆过滤器中[用户获得点赞数变更记录]
    }
}
