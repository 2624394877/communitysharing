package com.taoxin.communitysharing.content.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author taoxin
 * @description: 点赞/取消点赞MQDTO 用于更新数据库
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeUnlikeMQDTO {

    private Long userId;

    private Long contentId;

    private LocalDateTime createTime;

    private Integer status; // 1: 点赞, 0: 取消点赞

    private Long creatorId; // 创建者ID
}
