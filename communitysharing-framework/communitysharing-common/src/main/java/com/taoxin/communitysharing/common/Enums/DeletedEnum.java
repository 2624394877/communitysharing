package com.taoxin.communitysharing.common.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeletedEnum {
    YES(true), // 已删除
    NO(false); // 未删除

    private Boolean value; // 值
}
