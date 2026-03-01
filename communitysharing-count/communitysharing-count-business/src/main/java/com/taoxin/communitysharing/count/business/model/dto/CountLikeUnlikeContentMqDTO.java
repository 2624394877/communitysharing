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
public class CountLikeUnlikeContentMqDTO {
    private Long userId;

    private Long contentId;

    /**
     * 0: 取消点赞， 1：点赞
     */
    private Integer status;

    private LocalDateTime createTime;

    private Long creatorId;
}
