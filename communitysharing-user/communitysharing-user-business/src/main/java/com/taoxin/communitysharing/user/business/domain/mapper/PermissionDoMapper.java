package com.taoxin.communitysharing.user.business.domain.mapper;

import com.taoxin.communitysharing.user.business.domain.databaseObject.PermissionDo;

import java.util.List;

public interface PermissionDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PermissionDo record);

    int insertSelective(PermissionDo record);

    PermissionDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PermissionDo record);

    int updateByPrimaryKey(PermissionDo record);

    /**
     * 获取所有启用的权限
     * @return 启用的权限
     */
    List<PermissionDo> selectEnabledList();
}