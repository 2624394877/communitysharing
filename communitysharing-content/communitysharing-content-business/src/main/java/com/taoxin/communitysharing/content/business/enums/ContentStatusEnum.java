package com.taoxin.communitysharing.content.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ContentStatusEnum {
    BE_AUDIT(0), // 待审核
    NORMAL(1), // 正常
    DELETED(2), // 删除
    BE_UNDERCARRIAGE(3), // 被下架
    ;
    private final Integer code;
}
