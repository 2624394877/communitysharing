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
public class UserUpdateMailReqVo {
    @NotNull(message = "邮箱不能为空")
    private String email;
    private String code;
}
