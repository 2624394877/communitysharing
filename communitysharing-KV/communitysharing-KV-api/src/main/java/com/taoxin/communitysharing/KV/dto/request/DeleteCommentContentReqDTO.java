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
public class DeleteCommentContentReqDTO {

    @NotNull(message = "内容 ID 不能为空")
    private Long contentId;

    @NotBlank(message = "发布年月不能为空")
    private String yearMonth;

    @NotBlank(message = "评论内容不能为空")
    private String commentId; // 评论内容 uuid
}
