package com.taoxin.communitysharing.KV.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddBatchCommentContentReqDTO {
    @NotEmpty(message = "批量添加评论内容不能为空")
    @Valid // 指定集合内的评论 DTO，也需要进行参数校验
    List<CommentContentReqDTO> comments;
}
