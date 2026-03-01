package com.taoxin.communitysharing.algin.domain.mapper;

import org.apache.ibatis.annotations.Param;

public interface InsertMapper {

    /**
     * 笔记点赞数：计数变更
     */
    void insertDataAlignContentLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("contentId") Long contentId);

    /**
     * 用户获得的点赞数：计数变更
     */
    void insertDataAlignUserLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") Long userId);

    /**
     * 笔记收藏数：计数变更
     */
    void insertDataAlignContentCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("contentId") Long contentId);

    /**
     * 用户收藏数：计数变更
     */
    void insertDataAlignUserCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") Long userId);

    /**
     * 笔记发布数：增量表插入
     */
    void insertDataAlignContentPublishCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") Long userId);

    /**
     * 用户关注数：增量表插入
     * @param tableNameSuffix 表名后缀
     * @param userId 用户id
     */
    void insertDataAlignContentFollowCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") Long userId);

    /**
     * 用户粉丝数：增量表插入
     * @param tableNameSuffix 表名后缀
     * @param userId 用户id
     */
    void insertDataAlignContentFanCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") Long userId);
}
