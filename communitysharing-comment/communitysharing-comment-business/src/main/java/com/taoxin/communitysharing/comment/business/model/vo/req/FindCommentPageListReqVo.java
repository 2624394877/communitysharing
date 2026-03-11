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
public class FindCommentPageListReqVo {
    @NotNull(message = "内容ID不能为空")
    private String contentId;
    @NotNull(message = "页码不能为空")
    private Integer pageNo;
}
