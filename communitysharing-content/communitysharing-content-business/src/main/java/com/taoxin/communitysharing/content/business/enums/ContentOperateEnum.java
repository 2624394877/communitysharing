package com.taoxin.communitysharing.content.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 笔记操作枚举
 */
@Getter
@AllArgsConstructor
public enum ContentOperateEnum {
    PUBLISH(1),
    DELETED(0),
    ;
    private final Integer code;
}
