package com.taoxin.communitysharing.search.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentVisibaleEnum {
    PUBLIC(0), // 公开
    PRIVATE(1), // 私密
    ;

    private final Integer code;
}
