package com.taoxin.communitysharing.comment.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentLikeLuaResultEnum {
    NOT_EXIST(-1L), // 不存在
    LIKED(1L), // 已点赞
    LIKE_SUCCESS(0L); // 点赞成功

    public final Long code;

    public static CommentLikeLuaResultEnum getByCode(Long code) {
        for (CommentLikeLuaResultEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
