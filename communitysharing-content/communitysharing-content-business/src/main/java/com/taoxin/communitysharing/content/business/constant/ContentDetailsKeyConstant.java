package com.taoxin.communitysharing.content.business.constant;

public class ContentDetailsKeyConstant {
    private static final String CONTENT_DETAILS_KEY_PREFIX = "content:details:";

    /**
     * redis中用户的内容点赞列表，使用bloom过滤器
     */
    private static final String BLOOM_USER_CONTENT_LIKE_LIST_KEY = "bloom:content:likes:";

    /**
     * redis中用户的内容点赞列表
     */
    private static final String BLOOM_USER_CONTENT_LIKE_ZSET_KEY = "user:content:likes:";

    /**
     * redis中用户的内容收藏列表，使用bloom过滤器
     */
    private static final String BLOOM_USER_CONTENT_COLLECT_LIST_KEY = "bloom:content:collects:";

    private static final String BLOOM_USER_CONTENT_COLLECT_ZSET_KEY = "user:content:collects:";

    public static String getContentDetailsKey(Long contentId) {
        return CONTENT_DETAILS_KEY_PREFIX + contentId;
    }

    /**
     * Redis Key for Likeing User
     * @param userId 点赞的用户
     * @return redisKey
     */
    public static String getBloomUserContentLikeListKey(Long userId) {
        return BLOOM_USER_CONTENT_LIKE_LIST_KEY + userId;
    }

    /**
     * Redis Key for Likeing User
     * @param userId 点赞的用户
     * @return redisKey
     */
    public static String getBloomUserContentLikeZSetKey(Long userId) {
        return BLOOM_USER_CONTENT_LIKE_ZSET_KEY + userId;
    }

    public static String getBloomUserContentCollectListKey(Long userId) {
        return BLOOM_USER_CONTENT_COLLECT_LIST_KEY + userId;
    }

    public static String getBloomUserContentCollectZSetKey(Long userId) {
        return BLOOM_USER_CONTENT_COLLECT_ZSET_KEY + userId;
    }
}
