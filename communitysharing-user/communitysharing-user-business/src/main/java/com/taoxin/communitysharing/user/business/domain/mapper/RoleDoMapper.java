package com.taoxin.communitysharing.user.business.domain.mapper;


import com.taoxin.communitysharing.user.business.domain.databaseObject.RoleDo;

import java.util.List;

public interface RoleDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RoleDo record);

    int insertSelective(RoleDo record);

    RoleDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RoleDo record);

    int updateByPrimaryKey(RoleDo record);

    List<RoleDo> selectEnabledRolesList();
}