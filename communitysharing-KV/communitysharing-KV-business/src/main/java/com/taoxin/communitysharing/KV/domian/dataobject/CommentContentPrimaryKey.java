package com.taoxin.communitysharing.KV.domian.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@PrimaryKeyClass
public class CommentContentPrimaryKey {
    @PrimaryKeyColumn(name = "content_id", type = PrimaryKeyType.PARTITIONED)
    private Long contentId; // 分区键
    @PrimaryKeyColumn(name = "year_month", type = PrimaryKeyType.PARTITIONED)
    private String yearMonth; // 分区键
    @PrimaryKeyColumn(name = "comment_id", type = PrimaryKeyType.CLUSTERED)
    private UUID commentId; // 聚簇键
}
