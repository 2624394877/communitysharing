package com.taoxin.communitysharing.content.business.domain.mapper;

import com.taoxin.communitysharing.content.business.domain.databaseObject.TopicDo;

public interface TopicDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TopicDo record);

    int insertSelective(TopicDo record);

    TopicDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TopicDo record);

    int updateByPrimaryKey(TopicDo record);

    String getTopicNameByTopicId(Long id);
}