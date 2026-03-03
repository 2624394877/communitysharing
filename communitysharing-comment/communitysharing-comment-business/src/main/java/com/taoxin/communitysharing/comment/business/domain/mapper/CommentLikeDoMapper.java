package com.taoxin.communitysharing.comment.business.domain.mapper;

import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentLikeDo;
import com.taoxin.communitysharing.comment.business.model.dto.LikeUnLikeCommentDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentLikeDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentLikeDo record);

    int insertSelective(CommentLikeDo record);

    CommentLikeDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentLikeDo record);

    int updateByPrimaryKey(CommentLikeDo record);

    int selectByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    List<Long> selectByCommentId(@Param("commentId") Long commentId);

    int batchDelete(@Param("unLike") List<LikeUnLikeCommentDTO> unLike);

    int batchInsert(@Param("like") List<LikeUnLikeCommentDTO> like);

    /**
     * 更新评论点赞数
     * @param count
     * @param commentId
     * @return
     */
    int updateLikeTotalByCommentId(@Param("count") Integer count,
                                   @Param("commentId") Long commentId);
}