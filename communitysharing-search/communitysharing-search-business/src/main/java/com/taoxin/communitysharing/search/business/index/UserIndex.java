package com.taoxin.communitysharing.search.business.index;

public class UserIndex {
    // 索引名称
    public static final String NAME = "user";
    // 用户id
    public static final String FIELD_USER_ID = "id";
    // 社区分享id
    public static final String FIELD_COMMUNITYSHARING_ID = "communitysharing_id";
    // 昵称
    public static final String FIELD_NICKNAME = "nickname";
    // 昵称前缀
    public static final String FIELD_NICKNAME_PREFIX = "username.prefix";
    // 头像
    public static final String FIELD_AVATAR = "avatar";
    // 粉丝数
    public static final String FIELD_FANS_COUNT = "fans_total";
    // 内容数
    public static final String FIELD_CONTENT_COUNT = "content_total";

    public static final String FIELD_LIKE_TOTAL = "like_total";
}
