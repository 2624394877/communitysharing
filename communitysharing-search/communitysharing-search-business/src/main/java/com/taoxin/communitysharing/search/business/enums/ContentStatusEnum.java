package com.taoxin.communitysharing.search.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentStatusEnum {
    BE_EXAMINE(0), // 待审核
    NORMAL(1), // 正常
    DELETED(2), // 删除
    DOWN(3), // 下架
    ;

    private final Integer code;
}
