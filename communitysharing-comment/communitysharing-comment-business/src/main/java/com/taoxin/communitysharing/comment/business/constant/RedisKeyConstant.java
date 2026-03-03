package com.taoxin.communitysharing.comment.business.constant;

public class RedisKeyConstant {

    /**
     * Key 前缀：布隆过滤器 - 用户点赞的评论
     */
    private static final String BLOOM_COMMENT_LIKES_KEY_PREFIX = "bloom:comment:likes:";

    public static String getBloomCommentLikesKey(Long commentId) {
        return BLOOM_COMMENT_LIKES_KEY_PREFIX + commentId;
    }
}
