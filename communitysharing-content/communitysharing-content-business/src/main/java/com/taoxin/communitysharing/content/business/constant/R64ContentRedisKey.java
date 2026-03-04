package com.taoxin.communitysharing.content.business.constant;

public class R64ContentRedisKey {
    private static final String R64_USER_CONTENT_LIKE_LIST_KEY = "r64:content:likes:";

    public static String getContentLikeKey(Long contentId) {
        return R64_USER_CONTENT_LIKE_LIST_KEY + contentId;
    }
}
