package com.taoxin.communitysharing.count.business.domain.mapper;

import com.taoxin.communitysharing.count.business.domain.databaseObject.CommentDo;
import org.apache.ibatis.annotations.Param;

public interface CommentDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentDo record);

    int insertSelective(CommentDo record);

    CommentDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentDo record);

    int updateByPrimaryKey(CommentDo record);

    int updateChildCommentTotal(@Param("parentId") Long parentId, @Param("count") int count);

    int updateLikeTotalByCommentId(@Param("count") Integer count,
                                   @Param("commentId") Long commentId);
}