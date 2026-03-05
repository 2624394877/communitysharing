package com.taoxin.communitysharing.content.business.constant;

public interface MQConstant {
    String TOPIC_DELETE_CONTENT_LOCAL_CACHE = "DeleteContentLocalCacheTopic"; // 删除内容缓存的Topic

    String TOPIC_DELAY_DELETE_CONTENT_LOCAL_CACHE = "DelayDeleteContentLocalCacheTopic"; // 延迟删除内容缓存的Topic

    /**
     * Topic: 点赞、取消点赞共用一个
     */
    String TOPIC_LIKE_OR_UNLIKE = "LikeUnlikeTopic";

    /**
     * 点赞标签
     */
    String TAG_LIKE = "Like";

    /**
     * 取消点赞标签
     */
    String TAG_UNLIKE = "Unlike";

    String  TOPIC_CONTENT_LIKE_COUNT_CHANGE = "ContentLikeCountChangeTopic"; // 内容点赞数变化的Topic

    String TOPIC_COLLECT_OR_UNCOLLECT = "CollectUnCollectTopic"; // 收藏/取消收藏的Topic

    String TAG_COLLECT = "Collect"; // 收藏标签

    String TAG_UNCOLLECT = "UnCollect"; // 取消收藏标签

    String  TOPIC_CONTENT_COLLECT_COUNT_CHANGE = "ContentCollectCountChangeTopic"; // 内容收藏数变化的Topic

    String TOPIC_CONTENT_OPERATION_RECORD = "ContentOperationRecordTopic"; // 内容操作记录的Topic

    String TAG_CREATE_CONTENT = "CreateContent"; // 创建内容标签

    String TAG_UPDATE_CONTENT = "UpdateContent"; // 更新内容标签

    String TAG_DELETE_CONTENT = "DeleteContent"; // 删除内容标签

    String TOPIC_DELAY_DELETE_PUBLISHED_CONTENT_LIST_REDIS_CACHE = "delayDeletePublishContentListRedisCache";
    String TOPIC_DELAY_DELETE_PUBLISHED_CONTENT_LIST_REDIS_CACHE_UPDATE = "delayDeletePublishContentListRedisCacheUpdate";
}
