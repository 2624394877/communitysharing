package com.taoxin.communitysharing.count.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountCollectUnCollectMqDTO {
    private Long userId;

    private Long contentId;

    /**
     * 1: 收藏， 0：取消收藏
     */
    private Integer status;

    private LocalDateTime createTime;

    private Long creatorId;
}
