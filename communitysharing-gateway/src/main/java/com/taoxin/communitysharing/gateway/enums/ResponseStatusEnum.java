package com.taoxin.communitysharing.gateway.enums;

import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseStatusEnum implements BaseExceptionInterface {
    ROLE_PERMISSION_DENIE("403", "权限不足"),
    SYSTEM_ERROR("500", "系统异常s"),
    NOT_ROLE("403", "未授权"),
    NOT_LOGIN("401", "需要先登录"),
    TOKEN_EXPIRED("401", "登录已过期，请重新登录"),
    ;

    private final String ErrorCode;
    private final String ErrorMessage;
}
