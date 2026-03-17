package com.taoxin.communitysharing.user.relation.buiness.domain.mapper;

import com.taoxin.communitysharing.user.relation.buiness.domain.databaseObject.FollowingDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FollowingDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FollowingDo record);

    int insertSelective(FollowingDo record);

    FollowingDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FollowingDo record);

    int updateByPrimaryKey(FollowingDo record);

    List<FollowingDo> selectByUserId(Long userId);

    int deleteByUserIdAndFolllowingUserId(@Param("userId") Long userId, @Param("unfollowingUserId") Long unfollowingUserId);

    long selectCountByUserId(Long userId);

    /**
     * 分页查询
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<FollowingDo> selectPageListByUserId(@Param("userId") Long userId,
                                             @Param("offset") long offset,
                                             @Param("limit") long limit);

    List<FollowingDo> selectAllByUserId(Long userId);

    int selectCountByUserIdAndFollowingUserId(@Param("userId") Long userId, @Param("followingUserId") Long followingUserId);

    List<FollowingDo> selectByFollowingsByUserId(@Param("userId") Long userId);
}