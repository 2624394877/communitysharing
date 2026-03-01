package com.taoxin.communitysharing.algin.constant;

public interface MQConstant {
    /**
     * 点赞计数主题
     */
    String TOPIC_COUNT_CONTENT_LIKE = "ContentLikeCountChangeTopic";

    /**
     * 收藏/取消收藏主题
     */
    String TOPIC_COLLECT_OR_UNCOLLECT = "ContentCollectCountChangeTopic";

    /**
     * 内容操作记录主题
     */
    String TOPIC_CONTENT_OPERATION_RECORD = "ContentOperationRecordTopic";

    /**
     * 关注/取消关注主题
     */
    String FOLLOW_COUNT = "FollowCountTopic";
}
