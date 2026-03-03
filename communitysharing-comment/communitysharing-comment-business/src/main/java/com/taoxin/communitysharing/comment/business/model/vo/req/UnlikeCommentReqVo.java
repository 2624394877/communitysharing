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
public class UnlikeCommentReqVo {
    @NotNull(message = "评论id不能为空")
    private Long commentId;
}
