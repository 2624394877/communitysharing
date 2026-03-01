package com.taoxin.communitysharing.comment.business.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentHotBo {
    /**
     * 评论id
     */
    private Long commentId;

    /**
     * 热度值
     */
    private double hot;
}
