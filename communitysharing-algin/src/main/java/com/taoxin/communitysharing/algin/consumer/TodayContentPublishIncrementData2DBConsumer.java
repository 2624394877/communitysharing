package com.taoxin.communitysharing.algin.consumer;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.algin.constant.MQConstant;
import com.taoxin.communitysharing.algin.constant.RedisKeysConstant;
import com.taoxin.communitysharing.algin.constant.TableConstant;
import com.taoxin.communitysharing.algin.domain.mapper.InsertMapper;
import com.taoxin.communitysharing.algin.job.config.TableShardConfig;
import com.taoxin.communitysharing.algin.model.dto.ContentUserPublishDTO;
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
        consumerGroup = "communitysharing_group_algin_" + MQConstant.TOPIC_CONTENT_OPERATION_RECORD,
        topic = MQConstant.TOPIC_CONTENT_OPERATION_RECORD
)
public class TodayContentPublishIncrementData2DBConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private TableShardConfig tableShardConfig;
    @Resource
    private InsertMapper insertMapper;

    @Override
    public void onMessage(String body) {
        log.info("【内容发布数每日表】消费者接收到消息: {}", body);
        ContentUserPublishDTO contentUserPublishDTO = JsonUtil.parseObject(body, ContentUserPublishDTO.class);
        if (Objects.isNull(contentUserPublishDTO)) return;
        Long creatorId = contentUserPublishDTO.getCreatorId();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = RedisKeysConstant.getBloomTodayUserPublishes(date);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/bloom_today_content_publish.lua")));
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(redisKey),creatorId);
        log.info("【内容发布数每日表】布隆过滤器{}测试: {}, creatorId: {}", redisKey, result, creatorId);

        if (Objects.equals(result,0L)) {
            Long userIdHashKey = creatorId % tableShardConfig.getTableShards();
            transactionTemplate.execute(status -> {
                try {
                    insertMapper.insertDataAlignContentPublishCountTempTable(TableConstant.buildTableNameSuffix(date,userIdHashKey), creatorId);
                    log.info("【内容发布数每日表】对齐数据入库: {}", TableConstant.buildTableNameSuffix(date,userIdHashKey));
                    return true;
                }catch (Exception e) {
                    log.error("【内容发布数每日表】数据库更新失败, userId: {}", creatorId, e);
                    status.setRollbackOnly();
                }
                return false;
            });
            // 更新布隆过滤器
            RedisScript<Long> bloomAddScript = RedisScript.of("return redis.call('BF.ADD', KEYS[1], ARGV[1])", Long.class);
            redisTemplate.execute(bloomAddScript, Collections.singletonList(redisKey),creatorId);
        }
    }
}
