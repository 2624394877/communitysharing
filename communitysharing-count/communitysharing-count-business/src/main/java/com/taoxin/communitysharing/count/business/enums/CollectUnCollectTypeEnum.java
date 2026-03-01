package com.taoxin.communitysharing.count.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CollectUnCollectTypeEnum {
    COLLECT(1),
    UNCOLLECTED(0);
    private final Integer code;

    public static CollectUnCollectTypeEnum getByCode(Integer code) {
        for (CollectUnCollectTypeEnum value : CollectUnCollectTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
