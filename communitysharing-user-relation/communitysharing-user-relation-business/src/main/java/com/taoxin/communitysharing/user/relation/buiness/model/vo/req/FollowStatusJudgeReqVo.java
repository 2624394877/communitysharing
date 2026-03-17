package com.taoxin.communitysharing.user.relation.buiness.model.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowStatusJudgeReqVo {
    @NotNull(message = "用户id不能为空")
    private String userId;
}
