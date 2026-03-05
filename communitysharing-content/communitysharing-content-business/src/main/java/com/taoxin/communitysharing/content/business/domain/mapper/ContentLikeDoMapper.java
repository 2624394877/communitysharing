package com.taoxin.communitysharing.content.business.domain.mapper;

import com.taoxin.communitysharing.content.business.domain.databaseObject.ContentLikeDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ContentLikeDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ContentLikeDo record);

    int insertSelective(ContentLikeDo record);

    ContentLikeDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ContentLikeDo record);

    int updateByPrimaryKey(ContentLikeDo record);

    int selectCountByUserIdAndContentId(@Param("userId") Long userId, @Param("contentId") Long contentId);

    int selectCountIsLiked(@Param("userId") Long userId, @Param("contentId") Long contentId);

    List<ContentLikeDo> selectContentsIdByUserId(@Param("userId") Long userId);

    /**
     * 根据用户id和限制数量查询点赞记录
     * @param userId 当前用户id
     * @param limit 限制数量
     * @return 点赞记录列表
     */
    List<ContentLikeDo> selectLikedByUserIdAndLimit(@Param("userId") Long userId, @Param("limit")  int limit);

    /**
     * 根据用户id和内容id查询点赞记录
     * @param record 包含用户id和内容id的记录
     * @return 点赞记录
     */
    int insertOrUpdate(ContentLikeDo record);

    int insertOrUpdateUnlike(ContentLikeDo record);

    int batchInsertOrUpdate(@Param("contentLikeDos") List<ContentLikeDo> contentLikeDos);

    List<ContentLikeDo> selectByContentIdAndContentIds(@Param("contentId") Long contentId, @Param("contentIds") List<Long> contentIds);
}