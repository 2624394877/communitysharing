package com.taoxin.communitysharing.algin.domain.mapper;

import org.apache.ibatis.annotations.Param;

public interface UpdateMapper {
    int updateUserCountFollowingTotal(@Param("userId") long userId, @Param("followingTotal") int followingTotal);

    int updateContentCountLikeTotal(@Param("contentId") long contentId, @Param("likeTotal") int likeTotal);

    int updateContentCountCollectTotal(@Param("contentId") long contentId, @Param("collectTotal") int collectTotal);

    int updateUserCountPublishTotal(@Param("creatorId") long creatorId, @Param("publishTotal") int publishTotal);

    int updateUserCountFansTotal(@Param("userId") long userId, @Param("fansTotal") int fansTotal);

    int updateUserCountLikeTotal(@Param("userId") long userId, @Param("likeTotal") int likeTotal);

    int updateUserCountCollectTotal(@Param("userId") long userId, @Param("collectTotal") int collectTotal);
}
