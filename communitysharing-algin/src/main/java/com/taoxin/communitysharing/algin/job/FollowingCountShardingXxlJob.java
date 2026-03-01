package com.taoxin.communitysharing.algin.job;

import cn.hutool.core.collection.CollUtil;
import com.taoxin.communitysharing.algin.constant.RedisKeysConstant;
import com.taoxin.communitysharing.algin.constant.TableConstant;
import com.taoxin.communitysharing.algin.domain.mapper.DeleteMapper;
import com.taoxin.communitysharing.algin.domain.mapper.DeleteTableMapper;
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
public class FollowingCountShardingXxlJob {
    @Resource
    private SelectMapper selectMapper;
    @Resource
    private UpdateMapper updateMapper;
    @Resource
    private DeleteMapper deleteMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @XxlJob("followingCountShardingJobHandler")
    public void followingCountShardingJobHandler() throws Exception {
        // 分片处理逻辑
        // 序号
        int shardingIndex = XxlJobHelper.getShardIndex();
        // 分片总数
        int shardingTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("分片序号：{}，分片总数：{}", shardingIndex, shardingTotal);
        log.info("分片序号：{}，分片总数：{}", shardingIndex, shardingTotal);

        // TODO 1. 分批次查询 t_data_align_following_count_temp_日期_分片序号，如一批次查询 1000 条，直到全部查询完成
        String date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tableNameSuffix = TableConstant.buildTableNameSuffix(date, shardingIndex); // 表名后缀
        int batchSize = 1000; // 批次大小
        int alginedTotal = 0; // 已处理的总数
        // TODO 2: 循环这一批发生变更的用户 ID， 对 t_following 关注表执行 count(*) 操作，获取总数
        while (true) {
            // 获取这一批数据
            List<Long> userIdList = selectMapper.selectBatchFromDataAlignFollowingCountTempTable(tableNameSuffix, batchSize);
            // 判空
            if (CollUtil.isEmpty(userIdList)) break;
            // 循环处理这批数据
            userIdList.forEach(userId -> {
                int followingTotal = selectMapper.selectCountFromFollowingTableByUserId(userId); // 获取总数
                // TODO 3: 更新 t_user_count 表中的 following_total 字段
                int count = updateMapper.updateUserCountFollowingTotal(userId, followingTotal);
                log.info("【用户关注数据对齐】数据库对齐成功，正在更新{}条数据。。。。",count);
                if (count > 0) {
                    log.info("【用户关注数据对齐】数据库对齐成功，正在进行缓存。。。。");
                    String userCountZSetKey = RedisKeysConstant.getUserCount(userId);
                    // 判断是否有这个集合
                    boolean userCountZSetExist = redisTemplate.hasKey(userCountZSetKey);
                    if (userCountZSetExist) {
                        redisTemplate.opsForHash().put(userCountZSetKey, RedisKeysConstant.FOLLOWING_TOTAL, followingTotal);
                    } // 集合处在缓存中，数据更新
                }
            });
            // TODO:4. 批量物理删除这一批次记录
            deleteMapper.batchDeleteDataAlignFollowingCountTempTable(TableConstant.buildTableNameSuffix(date, shardingIndex), userIdList);
            // 记录处理数
            alginedTotal += userIdList.size();
        }
        XxlJobHelper.log("已处理总数：{}", alginedTotal);
    }
}
