package com.taoxin.communitysharing.algin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentFollowUnfollowMQDTO {
    /**
     * 当前用户ID
     */
    private Long userId;

    /**
     * 目标用户ID
     */
    private Long targetUserId;

    /**
     * 1: 关注 0: 取消关注
     */
    private Integer type;
}
