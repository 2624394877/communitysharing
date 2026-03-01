package com.taoxin.communitysharing.algin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentLikeUnLikeMQDTO {
    private Long userId;

    private Long contentId;

    private LocalDateTime createTime;

    private Integer status; // 1: 点赞，0:取消点赞

    private Long creatorId;
}
