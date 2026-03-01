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
public class likeCountShardingXxlJob {
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

    @XxlJob("likeCountShardingJobHandler")
    public void likeCountShardingJobHandler() throws Exception {
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
        while (true){
            List<Long> contentIdList = selectMapper.selectBatchFromDataAlignLikeCountTempTable(tableNameSuffix, batchSize);
            log.info("【内容点赞数据对齐】开始对齐数据,tableNameSuffix: {}，contentIdList：{}" ,tableNameSuffix, contentIdList);
            if (CollUtil.isEmpty(contentIdList)) break;
            contentIdList.forEach(contentId -> {
                int LikeTotal = selectMapper.selectCountFromLikeTableByContentId(contentId);
                int count = updateMapper.updateContentCountLikeTotal(contentId, LikeTotal);
                log.info("【内容点赞数据对齐】开始对齐数据，数据库更新{}条记录", count);
                if (count > 0){
                    log.info("【内容点赞数据对齐】数据库对齐成功，正在进行缓存。。。。");
                    String contentCountZSetKey = RedisKeysConstant.getContentCount(contentId);
                    boolean contentCountZSetExist = redisTemplate.hasKey(contentCountZSetKey);
                    log.info("【内容数据对齐】{}缓存是否存在：{}",contentCountZSetKey,contentCountZSetExist);
                    if (contentCountZSetExist) {
                        redisTemplate.opsForHash().put(contentCountZSetKey, RedisKeysConstant.LIKED_TOTAL, LikeTotal);
                        log.info("【内容点赞数据对齐】{}缓存成功。。。。",contentCountZSetKey);
                    }
                }
                searchFeignApiService.rebuildContentDoc(contentId);
            });
            deleteMapper.batchDeleteDataAlignLikeCountTempTable(tableNameSuffix, contentIdList);
            alginedTotal += contentIdList.size();
        }
        XxlJobHelper.log("已处理总数：{}", alginedTotal);
        log.info("已处理总数：{}", alginedTotal);
    }
}
