package com.taoxin.communitysharing.count.business.domain.mapper;

import com.taoxin.communitysharing.count.business.domain.databaseObject.ContentLikeDo;

public interface ContentLikeDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ContentLikeDo record);

    int insertSelective(ContentLikeDo record);

    ContentLikeDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ContentLikeDo record);

    int updateByPrimaryKey(ContentLikeDo record);
}