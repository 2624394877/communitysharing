package com.taoxin.communitysharing.count.business.domain.mapper;

import com.taoxin.communitysharing.count.business.domain.databaseObject.UserCountDo;
import org.apache.ibatis.annotations.Param;

public interface UserCountDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserCountDo record);

    int insertSelective(UserCountDo record);

    UserCountDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserCountDo record);

    int updateByPrimaryKey(UserCountDo record);

    /**
     * 根据用户ID插入或更新粉丝总数。
     *
     * <p>逻辑说明：
     * <ul>
     *   <li>当 user_id 不存在时：插入一条记录，fans_total = count</li>
     *   <li>当 user_id 已存在时：在原有 fans_total 基础上累加 count</li>
     * </ul>
     *
     * <p>适用于关注/取关场景：
     * <ul>
     *   <li>count 为正数：增加粉丝数</li>
     *   <li>count 为负数：减少粉丝数</li>
     * </ul>
     *
     * @param count 变动数量（可为正或负）
     * @param userId 用户ID
     * @return 影响行数（插入返回1，更新通常返回2）
     */
    int insertOrUpdateFansTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);

    int insertOrUpdateFollowingTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);

    int insertOrUpdateLikeTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);

    int insertOrUpdateCollectTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);

    int insertOrUpdateContentTotalByUserId(@Param("count") long count, @Param("userId") Long userId);
}