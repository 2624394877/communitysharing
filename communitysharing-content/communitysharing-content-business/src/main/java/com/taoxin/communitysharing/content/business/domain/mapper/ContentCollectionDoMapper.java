package com.taoxin.communitysharing.content.business.domain.mapper;

import com.taoxin.communitysharing.content.business.domain.databaseObject.ContentCollectionDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ContentCollectionDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ContentCollectionDo record);

    int insertSelective(ContentCollectionDo record);

    ContentCollectionDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ContentCollectionDo record);

    int updateByPrimaryKey(ContentCollectionDo record);

    int selectCountByUserIdAndContentId(@Param("userId") Long userId, @Param("contentId") Long contentId);

    List<ContentCollectionDo> selectContentsByUserId(Long userId);

    List<ContentCollectionDo> selectCollectedByUserIdAndLimit(@Param("userId") Long userId, @Param("limit") int limit);

    int insertOrUpdate(ContentCollectionDo record);

    int batchInsertOrUpdate(@Param("contentCollectionDos") List<ContentCollectionDo> contentCollectionDos);
}