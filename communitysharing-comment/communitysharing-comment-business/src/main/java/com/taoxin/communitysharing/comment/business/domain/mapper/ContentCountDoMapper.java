package com.taoxin.communitysharing.comment.business.domain.mapper;

import com.taoxin.communitysharing.comment.business.domain.databaseObject.ContentCountDo;

public interface ContentCountDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ContentCountDo record);

    int insertSelective(ContentCountDo record);

    ContentCountDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ContentCountDo record);

    int updateByPrimaryKey(ContentCountDo record);

    Long selectCountCommentByContentId(Long contentId);
}