package com.taoxin.communitysharing.algin.job;

import cn.hutool.core.collection.CollUtil;
import com.taoxin.communitysharing.algin.constant.RedisKeysConstant;
import com.taoxin.communitysharing.algin.constant.TableConstant;
import com.taoxin.communitysharing.algin.domain.mapper.DeleteMapper;
import com.taoxin.communitysharing.algin.domain.mapper.SelectMapper;
import com.taoxin.communitysharing.algin.domain.mapper.UpdateMapper;
import com.taoxin.communitysharing.algin.rpc.SearchFeignApiService;
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
public class PublishCountShardingXxlJob {
    @Resource
    private SelectMapper selectMapper;
    @Resource
    private UpdateMapper updateMapper;
    @Resource
    private DeleteMapper deleteMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private SearchFeignApiService searchFeignApiService;

    @XxlJob("publishCountShardingJobHandler")
    public void publishCountShardingJobHandler() throws Exception {
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
            List<Long> creatorIdList = selectMapper.selectBatchFromDataAlignPublishCountTempTable(tableNameSuffix, batchSize);
            log.info("【用户发布数据对齐】开始对齐数据,tableNameSuffix: {}，creatorIdList：{}" ,tableNameSuffix, creatorIdList);
            if (CollUtil.isEmpty(creatorIdList)) break;
            creatorIdList.forEach(creatorId -> {
                int publishTotal = selectMapper.selectCountFromPublishTableByCreatorId(creatorId);
                int count = updateMapper.updateUserCountPublishTotal(creatorId, publishTotal);
                log.info("【用户发布数据对齐】开始对齐数据，数据库更新{}条记录", count);
                if (count > 0) {
                    log.info("【用户发布数据对齐】数据库对齐成功，正在进行缓存。。。。");
                    String userCountZSetKey = RedisKeysConstant.getUserCount(creatorId);
                    boolean userCountZSetExist = redisTemplate.hasKey(userCountZSetKey);
                    log.info("【用户数据对齐】{}缓存是否存在：{}",userCountZSetKey,userCountZSetExist);
                    if (userCountZSetExist) {
                        redisTemplate.opsForHash().put(userCountZSetKey, RedisKeysConstant.CONTENT_TOTAL, publishTotal);
                        log.info("【用户发布数据对齐】{}缓存成功。。。。",userCountZSetKey);
                    }
                }
                searchFeignApiService.rebuildUserDoc(String.valueOf(creatorId));
            });
            deleteMapper.batchDeleteDataAlignPublishCountTempTable(tableNameSuffix, creatorIdList);
            alginedTotal += creatorIdList.size();
        }
        XxlJobHelper.log("已处理总数：{}", alginedTotal);
        log.info("已处理总数：{}", alginedTotal);
    }
}
