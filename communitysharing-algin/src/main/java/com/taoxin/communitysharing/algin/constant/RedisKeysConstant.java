package com.taoxin.communitysharing.algin.constant;

public class RedisKeysConstant {

    public static final String BLOOM_TODAY_CONTENT_LIKES_CONTENT_ID = "bloom:Align:content:likes:contentIds:";

    public static final String BLOOM_TODAY_CONTENT_LIKES_CREATOR_ID = "bloom:Align:content:likes:creatorIds:";

    public static final String BLOOM_TODAY_CONTENT_COLLECTS_CONTENT_ID = "bloom:Align:content:collects:contentIds:";

    public static final String BLOOM_TODAY_CONTENT_COLLECTS_CREATOR_ID = "bloom:Align:content:collects:creatorIds:";

    public static final String BLOOM_TODAY_USER_PUBLISHES = "bloom:Align:user:publishes:";

    public static final String BLOOM_TODAY_USER_FOLLOWINGS = "bloom:Align:user:followings:";

    public static final String BLOOM_TODAY_USER_FANS = "bloom:Align:user:fans:";

    public static final String USER_COUNT = "user:count:"; // 用户统计

    public static final String FOLLOWING_TOTAL = "followingTotal";

    public static final String CONTENT_TOTAL = "contentTotal";

    public static final String FNAS_TOTAL = "fansTotal";

    public static final String LIKE_TOTAL = "likeTotal";

    public static final String COLLECT_TOTAL = "collectTotal";

    public static final String CONTENT_COUNT = "content:count:"; // 内容统计

    public static final String LIKED_TOTAL = "likedTotal";

    public static final String COLLECTED_TOTAL = "collectedTotal";

    public static String getBloomTodayContentLikesContentId(String date) {
        return BLOOM_TODAY_CONTENT_LIKES_CONTENT_ID + date;
    }

    public static String getBloomTodayContentLikesCreatorId(String date) {
        return BLOOM_TODAY_CONTENT_LIKES_CREATOR_ID + date;
    }

    public static String getBloomTodayContentCollectsContentId(String date) {
        return BLOOM_TODAY_CONTENT_COLLECTS_CONTENT_ID + date;
    }

    public static String getBloomTodayContentCollectsCreatorId(String date) {
        return BLOOM_TODAY_CONTENT_COLLECTS_CREATOR_ID + date;
    }

    public static String getBloomTodayUserPublishes(String date) {
        return BLOOM_TODAY_USER_PUBLISHES + date;
    }

    public static String getBloomTodayUserFollowings(String date) {
        return BLOOM_TODAY_USER_FOLLOWINGS + date;
    }

    public static String getBloomTodayUserFans(String date) {
        return BLOOM_TODAY_USER_FANS + date;
    }

    /**
     * redis的用户统计集合键
     * @param userId 用户id
     * @return 用户统计集合键
     */
    public static String getUserCount(long userId) {
        return USER_COUNT + userId;
    }

    /**
     * redis的内容统计集合键
     * @param contentId 内容id
     * @return 内容统计集合键
     */
    public static String getContentCount(long contentId) {
        return CONTENT_COUNT + contentId;
    }
}
