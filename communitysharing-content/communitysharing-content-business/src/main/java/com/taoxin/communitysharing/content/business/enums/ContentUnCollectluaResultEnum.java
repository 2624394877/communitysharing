package com.taoxin.communitysharing.content.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentUnCollectluaResultEnum {
    NOT_EXISTS(-1L),
    COLLECTED(1L),
    UNCOLLECTED(0L);
    private final Long code;

    public static ContentUnCollectluaResultEnum valueOf(Long code) {
        for (ContentUnCollectluaResultEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
