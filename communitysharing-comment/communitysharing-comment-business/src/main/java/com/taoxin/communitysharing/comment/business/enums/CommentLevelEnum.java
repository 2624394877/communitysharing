package com.taoxin.communitysharing.comment.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentLevelEnum {
    FIRST_LEVEL(1), // 一级评论
    SECOND_LEVEL(2), // 二级评论
    ;

    private final Integer code;
}
