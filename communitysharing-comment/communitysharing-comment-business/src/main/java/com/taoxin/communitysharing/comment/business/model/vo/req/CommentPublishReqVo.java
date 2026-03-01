package com.taoxin.communitysharing.comment.business.model.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentPublishReqVo {
    @NotNull(message = "内容不能为空")
    private Long contentId;

    // 评论内容
    private String content;

    // 评论图片
    private String imageUrl;

    // 回复的评论id
    private Long  replayCommentId;
}
