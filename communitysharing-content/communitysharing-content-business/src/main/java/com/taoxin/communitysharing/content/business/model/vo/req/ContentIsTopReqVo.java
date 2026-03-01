package com.taoxin.communitysharing.content.business.model.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentIsTopReqVo {
    @NotNull(message = "内容id不能为空")
    private Long contentId;

    @NotNull(message = "置顶状态不能为空")
    private boolean isTop;
}
