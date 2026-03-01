package com.taoxin.communitysharing.search.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentSortTypeEnum {
    DEFAULT(0), // 默认排序
    UPDATE_TIME_DESC(1), // 更新时间降序
    LIKE_COUNT_DESC(2), // 点赞数降序
    COMMENT_COUNT_DESC(3), // 评论数降序
    COLLECT_COUNT_DESC(4), // 收藏数降序
    ;

    private final int code;

    public static ContentSortTypeEnum getByCode(int code) {
        for (ContentSortTypeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
