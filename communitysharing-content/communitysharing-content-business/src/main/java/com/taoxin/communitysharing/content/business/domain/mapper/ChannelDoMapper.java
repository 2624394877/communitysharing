package com.taoxin.communitysharing.content.business.domain.mapper;

import com.taoxin.communitysharing.content.business.domain.databaseObject.ChannelDo;

public interface ChannelDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ChannelDo record);

    int insertSelective(ChannelDo record);

    ChannelDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ChannelDo record);

    int updateByPrimaryKey(ChannelDo record);
}