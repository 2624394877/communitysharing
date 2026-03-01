package com.taoxin.communitysharing.comment.business.domain.mapper;

import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentDo;
import com.taoxin.communitysharing.comment.business.model.bo.CommentBo;
import com.taoxin.communitysharing.comment.business.model.bo.CommentHotBo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentDo record);

    int insertSelective(CommentDo record);

    CommentDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentDo record);

    int updateByPrimaryKey(CommentDo record);

    /**
     * 根据评论 ID 批量查询
     * @param commentIds 评论 ID 列表
     * @return 评论列表
     */
    List<CommentDo> selectByCommentIds(@Param("commentIds") List<Long> commentIds);

    int batchInsert(@Param("comments") List<CommentBo> comments);

    int bacthUpdateCommentHeat(@Param("commentIds") List<Long> commentIds,@Param("commentHeatMap") List<CommentHotBo> commentHeatMap);

    CommentDo selectEarliestByParentId(Long parentId);

    int updateFirstReplyCommentIdByPrimaryKey(@Param("firstReplyCommentId") Long firstReplyCommentId, @Param("commentId") Long commentId);
}