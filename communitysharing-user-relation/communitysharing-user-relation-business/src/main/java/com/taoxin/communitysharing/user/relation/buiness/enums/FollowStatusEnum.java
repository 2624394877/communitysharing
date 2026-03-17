package com.taoxin.communitysharing.user.relation.buiness.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FollowStatusEnum {
    NOT_EXOST(-1L),
    NOT_FOLLOW(0L),
    FOLLOW(1L);
    private final Long code;

    public static FollowStatusEnum getByCode(Long code) {
        for (FollowStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
