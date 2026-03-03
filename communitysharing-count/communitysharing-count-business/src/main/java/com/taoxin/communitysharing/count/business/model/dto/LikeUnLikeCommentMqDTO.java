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
public class LikeUnLikeCommentMqDTO {
    private Long commentId;

    private Long userId;

    /**
     *  1:like 0:unlike
     */
    private  Integer type;

    private LocalDateTime createTime;
}
