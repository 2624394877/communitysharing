package com.taoxin.communitysharing.algin.domain.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DeleteTableMapper {
    /**
     * 删除日增表 关注
     */
    void deleteDataAlignFollowingCountTempTable(String tableNameSuffix);

    /**
     * 删除日增表 粉丝
     */
    void deleteDataAlignFansCountTempTable(String tableNameSuffix);

    /**
     * 删除日增表 收藏
     */
    void deleteDataAlignCollectCountTemTable(String tableNameSuffix);

    /**
     * 删除日增表 点赞
     */
    void deleteDataAlignLikeCountTemTable(String tableNameSuffix);

    /**
     * 删除日增表 用户获得收藏
     */
    void deleteDataAlignUserCollectCountTemTable(String tableNameSuffix);

    /**
     * 删除日增表 用户获得点赞
     */
    void deleteDataAlignUserLikeCountTemTable(String tableNameSuffix);

    /**
     * 删除日增表 用户发布数
     */
    void deleteDataAlignUserPublishCountTemTable(String tableNameSuffix);
}
