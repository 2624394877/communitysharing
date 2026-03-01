package com.taoxin.communitysharing.count.business.domain.mapper;

import com.taoxin.communitysharing.count.business.domain.databaseObject.ContentCountDo;
import org.apache.ibatis.annotations.Param;

public interface ContentCountDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ContentCountDo record);

    int insertSelective(ContentCountDo record);

    ContentCountDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ContentCountDo record);

    int updateByPrimaryKey(ContentCountDo record);

    void insertOrUpdateCollectTotalByContentId(@Param("contentId") Long contentId,@Param("collectTotal") Integer collectTotal);

    void insertOrUpdateLikeTotalByContentId(@Param("contentId") Long contentId, @Param("likeTotal") Integer likeTotal);
}