package com.taoxin.communitysharing.comment.business.constant;

public class CountConentRedisKeyConstant {
    /**
     * 内容维度计数 Key 前缀
     */
    private static final String COUNT_CONTENT_KEY_PREFIX = "count:content:";

    public static final String COMMENT_TOTAL = "commentTotal";

    public static final String LEVEL1_COMMENT_TOTAL = "level1commentTotal";

    /**
     * 评论维度计数 Key 前缀
     */
    private static final String COUNT_COMMENT_KEY_PREFIX = "count:comment:";

    public static final String LIKE_TOTAL = "likeTotal";

    public static final String CHILD_COMMENT_TOTAL = "childCommentTotal";

    public static String getCountContentKeyPrefix(Long contentId) {
        return COUNT_CONTENT_KEY_PREFIX + contentId;
    }

    public static String getCountCommentKeyPrefix(Long commentId) {
        return COUNT_COMMENT_KEY_PREFIX + commentId;
    }
}
