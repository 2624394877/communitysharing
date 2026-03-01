package com.taoxin.communitysharing.comment.business.domain.mapper;

import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentLikeDo;

public interface CommentLikeDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentLikeDo record);

    int insertSelective(CommentLikeDo record);

    CommentLikeDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentLikeDo record);

    int updateByPrimaryKey(CommentLikeDo record);
}