package com.taoxin.communitysharing.search.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentTimeRangeEnum {
    DAY(0),
    WEEK(1),
    MONTH(2),
    HALF_YEAR(3),
    ;

    private final int code;

    public static ContentTimeRangeEnum getByCode(int code) {
        for (ContentTimeRangeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
