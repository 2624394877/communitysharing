package com.taoxin.communitysharing.algin.consumer;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.algin.constant.MQConstant;
import com.taoxin.communitysharing.algin.constant.RedisKeysConstant;
import com.taoxin.communitysharing.algin.constant.TableConstant;
import com.taoxin.communitysharing.algin.domain.mapper.InsertMapper;
import com.taoxin.communitysharing.algin.job.config.TableShardConfig;
import com.taoxin.communitysharing.algin.model.dto.ContentCollectionUnCollectionMQDTO;
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
        consumerGroup = "communitysharing_group_algin_" + MQConstant.TOPIC_COLLECT_OR_UNCOLLECT,
        topic = MQConstant.TOPIC_COLLECT_OR_UNCOLLECT
)
public class TodayContentCollectIncrementData2DBConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private TableShardConfig tableShardConfig;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private InsertMapper insertMapper;

    @Override
    public void onMessage(String body) {
        log.info("【收藏数据对齐】消费到计数消息： {}", body);
        ContentCollectionUnCollectionMQDTO contentCollectionUnCollectionMQDTO = JsonUtil.parseObject(body, ContentCollectionUnCollectionMQDTO.class);
        if (Objects.isNull(contentCollectionUnCollectionMQDTO)) return;
        Long contentId = contentCollectionUnCollectionMQDTO.getContentId();
        Long creatorId = contentCollectionUnCollectionMQDTO.getCreatorId();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));


        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_today_content_collect_check.lua")));
        script.setResultType(Long.class);

        String ContentRedisKey = RedisKeysConstant.getBloomTodayContentCollectsContentId(date);
        Long result = redisTemplate.execute(script, Collections.singletonList(ContentRedisKey),contentId);
        RedisScript<Long> bloomAddScript = RedisScript.of("return redis.call('BF.ADD', KEYS[1], ARGV[1])", Long.class);
        log.info("【收藏数据对齐】 数据测试 contentId: {}, date: {}, 布隆过滤器: {}, lua返回值: {}",contentId, date, ContentRedisKey, result);
        if (Objects.equals(result,0L)) {
            long contentIdHashKey = contentId % tableShardConfig.getTableShards();
            try {
                insertMapper.insertDataAlignContentCollectCountTempTable(TableConstant.buildTableNameSuffix(date,contentIdHashKey), contentId);
                log.info("【收藏数据对齐】对齐数据入库成功");
            } catch (Exception e) {
                log.error("【收藏数据对齐】对齐数据入库失败",e);
            }
            redisTemplate.execute(bloomAddScript, Collections.singletonList(ContentRedisKey),contentId);
        }

        String userRedisKey = RedisKeysConstant.getBloomTodayContentCollectsCreatorId(date);
        result = redisTemplate.execute(script, Collections.singletonList(userRedisKey),creatorId);
        log.info("【收藏数据对齐】 数据测试 creatorId: {}, date: {}, 布隆过滤器: {}, lua返回值: {}", creatorId, date, userRedisKey, result);
        if (Objects.equals(result,0L)) {
            long userIdHashKey = creatorId % tableShardConfig.getTableShards();
            try {
                insertMapper.insertDataAlignUserCollectCountTempTable(TableConstant.buildTableNameSuffix(date,userIdHashKey), creatorId);
            } catch (Exception e) {
                log.error("【用户获得收藏数据对齐】对齐数据入库失败",e);
            }
            redisTemplate.execute(bloomAddScript, Collections.singletonList(userRedisKey),creatorId);
        }
    }
}
