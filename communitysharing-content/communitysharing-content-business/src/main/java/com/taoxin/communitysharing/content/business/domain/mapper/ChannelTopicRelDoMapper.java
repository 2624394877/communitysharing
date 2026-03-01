package com.taoxin.communitysharing.content.business.domain.mapper;

import com.taoxin.communitysharing.content.business.domain.databaseObject.ChannelTopicRelDo;

public interface ChannelTopicRelDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ChannelTopicRelDo record);

    int insertSelective(ChannelTopicRelDo record);

    ChannelTopicRelDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ChannelTopicRelDo record);

    int updateByPrimaryKey(ChannelTopicRelDo record);
}