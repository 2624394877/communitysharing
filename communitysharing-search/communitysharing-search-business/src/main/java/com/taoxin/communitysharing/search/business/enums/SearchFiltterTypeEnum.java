package com.taoxin.communitysharing.search.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchFiltterTypeEnum {
    DEFAULT(0), // 默认
    CONTENT(1), // 内容
    USER(2); // 用户

    private final Integer code;

    public static SearchFiltterTypeEnum getByCode(Integer code) {
        for (SearchFiltterTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
