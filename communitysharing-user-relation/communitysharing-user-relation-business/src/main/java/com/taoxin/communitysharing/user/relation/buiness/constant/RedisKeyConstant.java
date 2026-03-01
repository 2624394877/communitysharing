package com.taoxin.communitysharing.user.relation.buiness.constant;

public class RedisKeyConstant {
    private static final String USER_FOLLOW_RELATION_KEY_PREFIX = "following:";

    private static final String USER_FANS_RELATION_KEY_PREFIX = "fans:";

    public static String getUserFollowRelationKey(Long userId) {
        return USER_FOLLOW_RELATION_KEY_PREFIX + userId;
    }

    public static String getUserFansRelationKey(Long fansUserId) {
        return USER_FANS_RELATION_KEY_PREFIX + fansUserId;
    }
}
