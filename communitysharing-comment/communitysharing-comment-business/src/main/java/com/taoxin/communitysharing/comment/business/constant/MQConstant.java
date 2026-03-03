package com.taoxin.communitysharing.comment.business.constant;

public interface MQConstant {
    String TOPIC_PUBLISH_COMMENT = "topicPublishComment";

    String TOPIC_COUNT_COMMENT = "topicCountComment";

    String TOPIC_COUNT_HEAT_UPDATE = "CountHeatUpdateTopic"; // 热度更新

    String TOPIC_COMMENT_LIKE = "commentLikeTopic";

    String TOPIC_COMMENT_LIKE_TAG = "liked";

    String TOPIC_COMMENT_UNLIKE_TAG = "unliked";

    String TOPIC_COUNT_COMMENT_LIKE_2_DB = "CountCommentLike2DBTopic";

    String TOPIC_COMMENT_DELETE = "CommentDeleteTopic";

    String TOPIC_COMMENT_DELETE_COUNT = "CommentDeleteTopicCount";
}
