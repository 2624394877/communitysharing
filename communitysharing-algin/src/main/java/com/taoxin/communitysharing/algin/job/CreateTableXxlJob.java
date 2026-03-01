package com.taoxin.communitysharing.algin.job;

import com.taoxin.communitysharing.algin.constant.TableConstant;
import com.taoxin.communitysharing.algin.domain.mapper.CreateTableMapper;
import com.taoxin.communitysharing.algin.job.config.TableShardConfig;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 定时自动创建日增量计数变更表
 */
@Component
public class CreateTableXxlJob {
    @Resource
    private TableShardConfig tableShardConfig;
    @Resource
    private CreateTableMapper createTableMapper;

    /**
     * @description @XxlJob(value="自定义 jobhandler 名称", init = "JobHandler 初始化方法", destroy = "JobHandler 销毁方法")
     * value值对应的是调度中心新建任务的 JobHandler 属性的值
     * 默认任务结果为 "成功" 状态，不需要主动设置
     * 可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果
     *
     *
     */
    @XxlJob("createTableHandle")
    public void createTableJobHandler() throws Exception {
        // TODO
        int tableShards = tableShardConfig.getTableShards();
        String date = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        XxlJobHelper.log("## 开始初始化明日增量数据表...");
        if (tableShards > 0) {
            // todo 如果表的分片数大于0，则
            for (int hashkey = 0; hashkey < tableShards; hashkey++) {
                String tableNameSuffix = TableConstant.buildTableNameSuffix(date, hashkey);
                createTableMapper.createDataAlignFollowingCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignFansCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignLikeCountTemTable(tableNameSuffix);
                createTableMapper.createDataAlignCollectCountTemTable(tableNameSuffix);
                createTableMapper.createDataAlignUserLikeCountTemTable(tableNameSuffix);
                createTableMapper.createDataAlignUserCollectCountTemTable(tableNameSuffix);
                createTableMapper.createDataAlignUserPublishCountTemTable(tableNameSuffix);
            }
        }
        XxlJobHelper.log("## 明日增量表创建完成，日期：{}",date);
    }
}
