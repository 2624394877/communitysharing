package com.taoxin.communitysharing.auth.model.vo.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginRequestVO {
    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String code;

    private String password;

    /**
     * 登录方式
     */
    @NotNull(message = "登录方式不能为空")
    private Integer type;
}
