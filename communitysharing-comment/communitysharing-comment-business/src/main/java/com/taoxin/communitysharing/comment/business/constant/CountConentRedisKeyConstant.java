package com.taoxin.communitysharing.comment.business.constant;

public class CountConentRedisKeyConstant {
    /**
     * 内容维度计数 Key 前缀
     */
    private static final String COUNT_CONTENT_KEY_PREFIX = "count:content:";

    public static final String COMMENT_TOTAL = "commentTotal";

    public static String getCountContentKeyPrefix(Long contentId) {
        return COUNT_CONTENT_KEY_PREFIX + contentId;
    }
}
