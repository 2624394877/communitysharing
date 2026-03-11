package com.taoxin.communitysharing.search.business.model.vo.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetContentByChannelReqVo {
    @NotNull(message = "channelId不能为空")
    private Long channelId;

    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1;
}
