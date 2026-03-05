package com.taoxin.communitysharing.content.business.domain.mapper;

import com.taoxin.communitysharing.content.business.domain.databaseObject.ContentDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ContentDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ContentDo record);

    int insertSelective(ContentDo record);

    ContentDo selectByPrimaryKey(Long id);

    int selectCountByContentId(Long contentId);

    int updateByPrimaryKeySelective(ContentDo record);

    int updateByPrimaryKey(ContentDo record);

    int updatePrivateOfVisibleByPrimaryKey(ContentDo record);

    int updateIsTopByPrimaryKey(ContentDo record);

    Long selectCreatorByContentId(Long contentId);

    List<ContentDo> selectPublishContentByUserIdAndCursor(@Param("userId") Long userId, @Param("cursor") Long cursor);
}