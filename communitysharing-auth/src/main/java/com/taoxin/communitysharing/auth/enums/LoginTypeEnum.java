package com.taoxin.communitysharing.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum LoginTypeEnum {
    PHONE_NUMBER(1), // 手机号登录
    PASSWORD(2); // 密码

    private final Integer value;

    public static LoginTypeEnum getByCode(Integer code) {
        for (LoginTypeEnum LoginType : LoginTypeEnum.values()) {
            if (Objects.equals(code,LoginType.getValue())) {
                return LoginType;
            }
        }
        return null;
    }
}
