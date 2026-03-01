package com.taoxin.communitysharing.content.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LikeUnlikeContentTypeEnum {
    LIKE(1), // 点赞
    UNLIKE(0); // 取消点赞

    private final Integer code;
}
