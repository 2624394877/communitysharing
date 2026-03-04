package com.taoxin.communitysharing.comment.business.domain.mapper;

import com.taoxin.communitysharing.comment.business.domain.databaseObject.ContentCountDo;
import org.apache.ibatis.annotations.Param;

public interface ContentCountDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ContentCountDo record);

    int insertSelective(ContentCountDo record);

    ContentCountDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ContentCountDo record);

    int updateByPrimaryKey(ContentCountDo record);

    Long selectCountCommentByContentId(Long contentId);

    int UpdateCommentCountByContentId(@Param("contentId") Long contentId, @Param("commentCount") int commentCount);
}