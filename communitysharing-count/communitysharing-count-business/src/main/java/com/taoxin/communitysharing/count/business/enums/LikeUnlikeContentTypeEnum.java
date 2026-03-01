package com.taoxin.communitysharing.count.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LikeUnlikeContentTypeEnum {
    LIKE(1),
    UNLIKE(0);

    private final Integer code;

    public static LikeUnlikeContentTypeEnum getByCode(Integer code) {
        for (LikeUnlikeContentTypeEnum value : LikeUnlikeContentTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
