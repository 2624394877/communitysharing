package com.taoxin.communitysharing.user.relation.buiness.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum LuaResultEnum {
    ZSET_NOT_EXIST(-1L), // zset不存在
    ZCARD_OVER_MAX_SIZE(-2L), // zset元素个数超过最大限制
    ZSCORE_FOLLOWED(-3L), // 用户已关注
    ZSCORE_NOT_FOLLOWED(-4L),
    ZADD_FOLLOWING(0L), // 用户已关注
    ;

    private final Long code;

    public static LuaResultEnum getByCode(Long code) {
        for (LuaResultEnum value : LuaResultEnum.values()) {
            if (Objects.equals(code, value.getCode())) {
                return value;
            }
        }
        return null;
    }
}
