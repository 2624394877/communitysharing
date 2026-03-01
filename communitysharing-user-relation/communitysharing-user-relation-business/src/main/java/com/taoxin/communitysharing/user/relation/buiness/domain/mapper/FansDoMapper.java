package com.taoxin.communitysharing.user.relation.buiness.domain.mapper;

import com.taoxin.communitysharing.user.relation.buiness.domain.databaseObject.FansDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FansDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FansDo record);

    int insertSelective(FansDo record);

    FansDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FansDo record);

    int updateByPrimaryKey(FansDo record);

    int deleteByUserIdAndFansUserId(@Param("userId") Long userId, @Param("fansUserId") Long fansUserId);

    long selectCountByUserId(Long userId);

    List<FansDo> selectPageListByUserId(@Param("userId") Long userId,
                                        @Param("offset") long offset,
                                        @Param("limit") long limit);
    List<FansDo> select5000FansByUserId(Long userId);
}