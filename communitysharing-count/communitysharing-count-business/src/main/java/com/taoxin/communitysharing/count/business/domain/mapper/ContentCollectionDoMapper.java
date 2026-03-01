package com.taoxin.communitysharing.count.business.domain.mapper;

import com.taoxin.communitysharing.count.business.domain.databaseObject.ContentCollectionDo;

public interface ContentCollectionDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ContentCollectionDo record);

    int insertSelective(ContentCollectionDo record);

    ContentCollectionDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ContentCollectionDo record);

    int updateByPrimaryKey(ContentCollectionDo record);
}