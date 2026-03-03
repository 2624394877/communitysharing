package com.taoxin.communitysharing.comment.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LikeUnLikeTypeEnum {
    LIKE(1), // 点赞
    UNLIKE(0); // 取消点赞

    public final Integer code;
}
