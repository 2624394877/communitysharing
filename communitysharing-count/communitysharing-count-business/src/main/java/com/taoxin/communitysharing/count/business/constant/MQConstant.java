package com.taoxin.communitysharing.count.business.constant;

public interface MQConstant {
    /**
     * 关注计数
     */
    String FOLLOW_COUNT = "FollowCountTopic";

    /**
     * 粉丝计数
     */
    String FANS_COUNT = "FansCountTopic";

    /**
     * Topic: 粉丝数计数入库
     */
    String TOPIC_COUNT_FANS_2_DB = "CountFans2DBTopic";

    /**
     * Topic: 粉丝数计数入库
     */
    String TOPIC_COUNT_FOLLOWING_2_DB = "CountFollowing2DBTopic";

    String  TOPIC_CONTENT_LIKE_COUNT_CHANGE = "ContentLikeCountChangeTopic"; // 内容点赞数变化的Topic

    String  TOPIC_CONTENT_LIKE_COUNT_CHANGE_2_DB = "ContentLikeCountChange2DBTopic"; // 内容点赞数变化入库的Topic

    String TOPIC_CONTENT_COLLECT_COUNT_CHANGE = "ContentCollectCountChangeTopic"; // 内容收藏数变化的Topic

    String TOPIC_CONTENT_COLLECT_COUNT_CHANGE_2_DB = "ContentCollectCountChange2DBTopic"; // 内容收藏数变化入库的Topic

    String TOPIC_CONTENT_OPERATION_RECORD = "ContentOperationRecordTopic";

    String TAG_CREATE_CONTENT = "CreateContent"; // 创建内容标签

    String TAG_UPDATE_CONTENT = "UpdateContent"; // 更新内容标签

    String TAG_DELETE_CONTENT = "DeleteContent"; // 删除内容标签

    String TOPIC_LIKE_OR_UNLIKE = "LikeUnlikeTopic"; // 点赞或取消点赞的Topic

    String TOPIC_COLLECT_OR_UNCOLLECT = "CollectUnCollectTopic"; // 收藏或取消收藏的Topic

    String TOPIC_COUNT_COMMENT = "topicCountComment"; // 评论数计数的Topic

    String TOPIC_COUNT_HEAT_UPDATE = "CountHeatUpdateTopic"; // 热度更新

    String TOPIC_COMMENT_LIKE = "commentLikeTopic";

    String TOPIC_COUNT_COMMENT_LIKE_2_DB = "CountCommentLike2DBTopic";
}
