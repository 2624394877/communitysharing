package com.taoxin.communitysharing.content.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentOperateMqDTO {
    private Long creatorId; // 创建者ID

    private Long contentId; // 内容ID

    private Integer operateType; // 1: 发布, 0: 删除
}
