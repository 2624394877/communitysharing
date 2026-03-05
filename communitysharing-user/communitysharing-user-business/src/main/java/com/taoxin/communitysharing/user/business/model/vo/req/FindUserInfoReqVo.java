package com.taoxin.communitysharing.user.business.model.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserInfoReqVo {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
