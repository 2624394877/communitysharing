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

    List<CommentDo> selectPageList(@Param("contentId") Long contentId,@Param("offset") long offset,@Param("pageSize") long pageSize);

    List<CommentDo> selectTwoLevelCommentByIds(@Param("commentIds") List<Long> commentIds);

    List<CommentDo> selectHotComments(@Param("contentId") Long contentId);

    Long selectSecondCommentCountByContentId(@Param("Leve1CommentId") Long Leve1CommentId);

    /**
     * 根据一级评论 ID 批量查询二级评论
     * @param Leve1CommentId
     * @param offset
     * @param pageSize
     * @return
     */
    List<CommentDo> selectSecondCommentByLeve1CommentId(@Param("Leve1CommentId") Long Leve1CommentId,@Param("offset") long offset,@Param("pageSize") long pageSize);

    /**
     * 根据评论 ID 批量查询评论数量
     * @param commentIds
     * @return
     */
    List<CommentDo> selectCommentCountByIds(@Param("commentIds") List<Long> commentIds);

    /**
     * 根据父级评论 ID 批量查询子评论
     * @param parentId 父级评论 ID
     * @param limit 查询数量
     * @return 排序后的数据，先时间降序，再点赞数降序
     */
    List<CommentDo> selectChildCommentsByParentIdAndLimit(@Param("parentId") Long parentId, @Param("limit") int limit);

    int DeleteByCommentId(@Param("commentId") Long commentId);

    /**
     * 批量删除评论
     * @param commentIds
     * @return
     */
    int deleteByIds(@Param("commentIds") List<Long> commentIds);

    /**
     * 根据 reply_comment_id 查询
     * @param commentId
     * @return
     */
    List<CommentDo> selectByReplyCommentId(@Param("commentId") Long commentId);

    int DeleteCommentByReplyCommentId(@Param("commentId") Long commentId);

    Long selectParentIdByCommentId(@Param("commentId") Long commentId);
}