package com.taoxin.communitysharing.KV.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentContentReqDTO {
    @NotNull(message = "评论id不能为空")
    private Long contentId;
    @NotBlank(message = "时间不能为空")
    private String yearMonth;
    @NotBlank(message = "正文id不能为空")
    private String commentId;
    @NotBlank(message = "正文不能为空")
    private String comment;
}
