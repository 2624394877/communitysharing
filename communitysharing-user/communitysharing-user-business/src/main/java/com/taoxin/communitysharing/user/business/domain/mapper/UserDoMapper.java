package com.taoxin.communitysharing.user.business.domain.mapper;


import com.taoxin.communitysharing.user.business.domain.databaseObject.UserDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserDo record);

    int insertSelective(UserDo record);

    UserDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDo record);

    int updateByPrimaryKey(UserDo record);

    UserDo selectByPhone(String phone);

    UserDo selectByEmail(String email);

    List<UserDo> selectById(@Param("ids") List<Long> ids);

    List<Long> selectAllUserIds();
}