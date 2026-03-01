package com.taoxin.communitysharing.content.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentUnlikeLuaResultEnum {
    NOT_EXISTS(-1L),
    LIKED(1L),
    UNLIKED(0L);

    private final Long code;

    public static ContentUnlikeLuaResultEnum valueOf(Long code) {
        for (ContentUnlikeLuaResultEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
