package com.taoxin.communitysharing.algin.job;

import com.taoxin.communitysharing.algin.constant.TableConstant;
import com.taoxin.communitysharing.algin.domain.mapper.DeleteTableMapper;
import com.taoxin.communitysharing.algin.job.config.TableShardConfig;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DeleteTableXxJob {
    @Resource
    private TableShardConfig tableShardConfig;
    @Resource
    private DeleteTableMapper deleteTableMapper;

    @XxlJob("deleteTableHandle")
    public void deleteTableJobHandler() throws Exception {
        int tableShards = tableShardConfig.getTableShards();
        String date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        XxlJobHelper.log("## 删除昨日数据表...");
        // 删除逻辑
        if (tableShards > 0) {
            for (int hashkey = 0; hashkey < tableShards; hashkey++) {
                String tableNameSuffix = TableConstant.buildTableNameSuffix(date, hashkey);
                deleteTableMapper.deleteDataAlignFollowingCountTempTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignFansCountTempTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignLikeCountTemTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignCollectCountTemTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignUserCollectCountTemTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignUserLikeCountTemTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignUserPublishCountTemTable(tableNameSuffix);
                XxlJobHelper.log("## 删除表完成...{}", tableNameSuffix);
            }
        }
        XxlJobHelper.log("## 删除昨日数据表完成..., 日期：{}", date);
    }
}
