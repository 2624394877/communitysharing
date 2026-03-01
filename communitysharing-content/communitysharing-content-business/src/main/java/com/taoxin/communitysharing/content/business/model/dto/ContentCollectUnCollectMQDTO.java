package com.taoxin.communitysharing.content.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentCollectUnCollectMQDTO {
    private Long userId;

    private Long contentId;

    private LocalDateTime createTime;

    private Integer status; // 1: 收藏, 0: 取消收藏

    private Long creatorId; // 创建者ID
}
