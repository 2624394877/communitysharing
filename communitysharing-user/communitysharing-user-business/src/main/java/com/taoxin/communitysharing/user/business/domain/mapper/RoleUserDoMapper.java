package com.taoxin.communitysharing.user.business.domain.mapper;


import com.taoxin.communitysharing.user.business.domain.databaseObject.RoleUserDo;

import java.util.List;

public interface RoleUserDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RoleUserDo record);

    int insertSelective(RoleUserDo record);

    RoleUserDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RoleUserDo record);

    int updateByPrimaryKey(RoleUserDo record);

    List<RoleUserDo> selectAll();
}