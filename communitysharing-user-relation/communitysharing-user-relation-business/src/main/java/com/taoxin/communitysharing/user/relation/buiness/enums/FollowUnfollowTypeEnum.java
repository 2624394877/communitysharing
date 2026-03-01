package com.taoxin.communitysharing.user.relation.buiness.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FollowUnfollowTypeEnum {

    FOLLOW(1), // 关注
    UNFOLLOW(0); // 取消关注

    private final Integer code;
}
