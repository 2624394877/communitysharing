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
public class CollectContentReqVo {
    /**
     * 内容id
     */
    @NotNull(message = "内容id不能为空")
    private String contentId;
}
