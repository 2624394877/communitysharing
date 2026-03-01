package com.taoxin.communitysharing.algin.consumer;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.algin.constant.MQConstant;
import com.taoxin.communitysharing.algin.constant.RedisKeysConstant;
import com.taoxin.communitysharing.algin.constant.TableConstant;
import com.taoxin.communitysharing.algin.domain.mapper.InsertMapper;
import com.taoxin.communitysharing.algin.job.config.TableShardConfig;
import com.taoxin.communitysharing.algin.model.dto.ContentFollowUnfollowMQDTO;
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
        consumerGroup = "communitysharing_group_algin_" + MQConstant.FOLLOW_COUNT,
        topic = MQConstant.FOLLOW_COUNT
)
public class TodayContentFollowingIncrementData2DBConsumer implements RocketMQListener<String> {
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
        log.info("【今日内容关注/粉丝数变化入库】消费者接收到消息: {}", body);
        ContentFollowUnfollowMQDTO contentFollowUnfollowMQDTO = JsonUtil.parseObject(body, ContentFollowUnfollowMQDTO.class);
        if (Objects.isNull(contentFollowUnfollowMQDTO)) return;
        Long userId = contentFollowUnfollowMQDTO.getUserId();
        Long targetUserId = contentFollowUnfollowMQDTO.getTargetUserId();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String followRedisKey = RedisKeysConstant.getBloomTodayUserFollowings(date);
        String fanRedisKey = RedisKeysConstant.getBloomTodayUserFans(date);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_today_content_follow_check.lua")));
        script.setResultType(Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(followRedisKey),userId);
        log.info("【今日内容关注数变化入库】数据测试 userId: {}, targetUserId: {}, date: {}, 布隆过滤器: {}, lua返回值: {}",userId, targetUserId, date, followRedisKey, result);
        if (Objects.equals(result,0L)) {
            Long userIdHashKey = userId % tableShardConfig.getTableShards();
            transactionTemplate.execute(status -> {
                try {
                    insertMapper.insertDataAlignContentFollowCountTempTable(TableConstant.buildTableNameSuffix(date,userIdHashKey), userId);
                    log.info("【今日内容关注数变化入库】数据入库成功, {}", TableConstant.buildTableNameSuffix(date,userIdHashKey));
                    return true;
                } catch (Exception e) {
                    log.error("【今日内容关注数变化入库】数据入库失败",e);
                    status.setRollbackOnly();
                }
                return false;
            });
            RedisScript<Long> bloomAddScript = RedisScript.of("return redis.call('BF.ADD', KEYS[1], ARGV[1])", Long.class);
            redisTemplate.execute(bloomAddScript, Collections.singletonList(followRedisKey),userId);
        }
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_today_content_fan_check.lua")));
        result = redisTemplate.execute(script, Collections.singletonList(fanRedisKey),userId);
        log.info("【今日内容粉丝数变化入库】数据测试 userId: {}, targetUserId: {}, date: {}, 布隆过滤器: {}, lua返回值: {}",userId, targetUserId, date, fanRedisKey, result);
        if (Objects.equals(result,0L)) {
            Long targetUserIdHashKey = targetUserId % tableShardConfig.getTableShards();
            transactionTemplate.execute(status -> {
                try {
                    insertMapper.insertDataAlignContentFanCountTempTable(TableConstant.buildTableNameSuffix(date,targetUserIdHashKey), targetUserId);
                    log.info("【今日内容粉丝数变化入库】数据入库成功, {}", TableConstant.buildTableNameSuffix(date,targetUserIdHashKey));
                    return true;
                } catch (Exception e) {
                    log.error("【今日内容粉丝数变化入库】数据入库失败",e);
                    status.setRollbackOnly();
                }
                return false;
            });
            RedisScript<Long> bloomAddScript = RedisScript.of("return redis.call('BF.ADD', KEYS[1], ARGV[1])", Long.class);
            redisTemplate.execute(bloomAddScript, Collections.singletonList(fanRedisKey),targetUserId);
        }
    }
}
