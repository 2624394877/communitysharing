package com.taoxin.communitysharing.common.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {
    ENABLED(0), // 启用
    DISABLED(1); // 禁用

    private final Integer code; // 状态码
}
