package com.taoxin.communitysharing.algin.job.config;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class TableShardConfig {
    @Value("${table.shards}")
    private int tableShards;

    public int getTableShards() {
        return tableShards;
    }
}
