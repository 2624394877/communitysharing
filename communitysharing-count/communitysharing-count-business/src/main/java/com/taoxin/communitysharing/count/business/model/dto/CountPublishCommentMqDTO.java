package com.taoxin.communitysharing.count.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountPublishCommentMqDTO {
    /**
     * 内容id
     */
    private Long contentId;

    /**
     * 评论id
     */
    private Long commentId;

    /**
     * 评论层级
     */
    private Integer level;

    /**
     * 父级评论id
     */
    private Long parentId;
}
