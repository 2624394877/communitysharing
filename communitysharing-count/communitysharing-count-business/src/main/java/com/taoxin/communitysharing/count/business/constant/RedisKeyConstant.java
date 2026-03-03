package com.taoxin.communitysharing.count.business.constant;

public class RedisKeyConstant {

    /**
     * 用户维度计数 Key 前缀
     */
    private static final String COUNT_USER_KEY_PREFIX = "count:user:";

    /**
     * Hash Field: 粉丝总数
     */
    public static final String FIELD_FANS_TOTAL = "fansTotal";

    /**
     * Hash Field: 关注总数
     */
    public static final String FIELD_FOLLOWING_TOTAL = "followingTotal";

    public static final String CONTENT_COUNT_TOTAL = "contentTotal";

    /**
     * 内容维度计数 Key 前缀
     */
    private static final String COUNT_CONTENT_KEY_PREFIX = "count:content:";

    /**
     * Hash Field: 内容点赞总数
     */
    public static final String FIELD_LIKE_TOTAL = "likeTotal";

    /**
     * Hash Field: 内容收藏总数
     */
    public static final String FIELD_COLLECT_TOTAL = "collectTotal";

    /**
     * 构建用户维度计数 Key
     * @param userId id
     * @return redisKey
     */
    public static String buildCountUserKey(Long userId) {
        return COUNT_USER_KEY_PREFIX + userId;
    }

    /**
     * 构建内容维度计数 Key
     * @param contentId 内容id
     * @return redisKey
     */
    public static String buildCountContentKey(Long contentId) {
        return COUNT_CONTENT_KEY_PREFIX + contentId;
    }
}
