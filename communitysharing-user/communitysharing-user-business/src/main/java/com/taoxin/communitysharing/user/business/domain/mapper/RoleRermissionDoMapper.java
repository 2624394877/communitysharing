package com.taoxin.communitysharing.user.business.domain.mapper;

import com.taoxin.communitysharing.user.business.domain.databaseObject.RoleRermissionDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleRermissionDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RoleRermissionDo record);

    int insertSelective(RoleRermissionDo record);

    RoleRermissionDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RoleRermissionDo record);

    int updateByPrimaryKey(RoleRermissionDo record);

    // import org.apache.ibatis.annotations.Param;注解
    List<RoleRermissionDo> selectByRoleIds(@Param("roleIds") List<Long> roleIds);
}