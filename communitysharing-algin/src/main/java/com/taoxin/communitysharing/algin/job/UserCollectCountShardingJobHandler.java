package com.taoxin.communitysharing.algin.job;

import cn.hutool.core.collection.CollUtil;
import com.taoxin.communitysharing.algin.constant.RedisKeysConstant;
import com.taoxin.communitysharing.algin.constant.TableConstant;
import com.taoxin.communitysharing.algin.domain.mapper.DeleteMapper;
import com.taoxin.communitysharing.algin.domain.mapper.SelectMapper;
import com.taoxin.communitysharing.algin.domain.mapper.UpdateMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class UserCollectCountShardingJobHandler {
    @Resource
    private SelectMapper selectMapper;
    @Resource
    private UpdateMapper updateMapper;
    @Resource
    private DeleteMapper deleteMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @XxlJob("userGetCollectCountShardingJobHandler")
    public void userGetCollectCountShardingJobHandler() throws Exception {
        // 分片处理逻辑
        // 序号
        int shardingIndex = XxlJobHelper.getShardIndex();
        // 分片总数
        int shardingTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("分片序号：{}，分片总数：{}", shardingIndex, shardingTotal);
        log.info("分片序号：{}，分片总数：{}", shardingIndex, shardingTotal);
        String date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tableNameSuffix = TableConstant.buildTableNameSuffix(date, shardingIndex); // 表名后缀
        int batchSize = 1000; // 批次大小
        int alginedTotal = 0; // 已处理的总数
        while (true) {
            List<Long> userIdList = selectMapper.selectBatchFromDataAlignUserCollectCountTempTable(tableNameSuffix, batchSize);
            log.info("【用户获得收藏数据对齐】开始对齐数据,tableNameSuffix: {}，userIdList：{}" ,tableNameSuffix, userIdList);
            if (CollUtil.isEmpty(userIdList)) break;
            userIdList.forEach(userId -> {
                int userCollectTotal = selectMapper.selectCountFromUserCollectTableByUserId(userId);
                int count = updateMapper.updateUserCountCollectTotal(userId, userCollectTotal);
                log.info("【用户获得收藏数据对齐】开始对齐数据，数据库更新{}条记录", count);
                if (count > 0) {
                    log.info("【用户获得收藏数据对齐】数据库对齐成功，正在进行缓存。。。。");
                    String userCountZSetKey = RedisKeysConstant.getUserCount(userId);
                    boolean userCountZSetExist = redisTemplate.hasKey(userCountZSetKey);
                    log.info("【用户获得收藏数据对齐】{}缓存是否存在：{}", userCountZSetKey, userCountZSetExist);
                    if (userCountZSetExist) {
                        redisTemplate.opsForZSet().add(userCountZSetKey, RedisKeysConstant.COLLECT_TOTAL, userCollectTotal);
                        log.info("【用户获得收藏数据对齐】缓存更新成功");
                    }
                }
            });
            deleteMapper.batchDeleteDataAlignUserCollectCountTempTable(tableNameSuffix, userIdList);
            alginedTotal += userIdList.size();
        }
        XxlJobHelper.log("【用户获得收藏数据对齐】缓存更新成功，已处理总数：{}", alginedTotal);
        log.info("【用户获得收藏数据对齐】缓存更新成功，已处理总数：{}", alginedTotal);
    }
}
