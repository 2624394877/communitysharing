package com.taoxin.communitysharing.comment.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentUnlikeLuaResultEnum {
    NOT_EXISTS(-1L),
    LIKED(1L),
    UNLIKED(0L);
    private final Long code;

    public static CommentUnlikeLuaResultEnum getByCode(Long code) {
        for (CommentUnlikeLuaResultEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
