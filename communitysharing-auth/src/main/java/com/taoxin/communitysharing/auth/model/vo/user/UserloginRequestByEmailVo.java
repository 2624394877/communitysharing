package com.taoxin.communitysharing.auth.model.vo.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserloginRequestByEmailVo {
    @NotBlank(message = "邮箱不能为空")
    private String email;

    private String code;
}
