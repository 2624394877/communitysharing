package com.taoxin.communitysharing.KV.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentContentReqDTO {
    /**
     * 发布年月
     */
    @NotBlank(message = "发布年月不能为空")
    private String yearMonth;

    /**
     * 评论内容 UUID
     */
    @NotBlank(message = "评论内容 UUID不能为空")
    private String commentId;
}
