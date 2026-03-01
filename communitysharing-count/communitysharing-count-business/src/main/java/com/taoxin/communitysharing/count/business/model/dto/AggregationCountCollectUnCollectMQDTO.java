package com.taoxin.communitysharing.count.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AggregationCountCollectUnCollectMQDTO {
    private Long creatorId; // 创建者ID

    private Long contentId; // 内容ID

    private Integer collectCount; // 收藏数量
}
