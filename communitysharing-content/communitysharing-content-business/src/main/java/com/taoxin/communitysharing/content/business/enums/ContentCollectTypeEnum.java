package com.taoxin.communitysharing.content.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentCollectTypeEnum {
    NOT_EXIST(-1L), // 不存在
    COLLECTED(1L), // 已收藏
    NOT_COLLECTED(0L); // 未收藏
    private Long code;

    public static ContentCollectTypeEnum valueOf(Long code) {
        for (ContentCollectTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null; // 默认返回不存在
    }
}
