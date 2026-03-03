package com.taoxin.communitysharing.comment.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeUnLikeCommentDTO {
    private Long commentId;

    private Long userId;

    /**
     *  1:like 0:unlike
     */
    private  Integer type;

    private LocalDateTime createTime;
}
