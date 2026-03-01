package com.taoxin.communitysharing.comment.business.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublishCommentMqDTO {
    @NotNull(message = "内容id不能为空")
    private Long contentId;

    private Long commentId; // 评论id

    // 评论内容
    private String content;

    // 评论图片
    private String imageUrl;

    // 回复的评论id
    private Long  replayCommentId;

    // 创建时间
    private LocalDateTime createTime;

    // 评论的人
    private Long creatorId;
}
