package com.taoxin.communitysharing.content.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ContentVisibleEnum {
    PUBLIC(0), // 公开
    PRIVATE(1), // 私密
    ;
    private final Integer code;
}
