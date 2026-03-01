package com.taoxin.communitysharing.algin.domain.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DeleteMapper {

    /**
     * 批量删除日增表 关注
     * @param tableNameSuffix
     * @param userIds
     */
    void batchDeleteDataAlignFollowingCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                     @Param("userIds") List<Long> userIds);

    void batchDeleteDataAlignLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                     @Param("contentIds") List<Long> contentIds);

    void batchDeleteDataAlignCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                     @Param("contentIds") List<Long> contentIds);

    void batchDeleteDataAlignPublishCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                     @Param("creatorIds") List<Long> creatorIds);

    void batchDeleteDataAlignFansCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                     @Param("userIds") List<Long> userIds);

    void batchDeleteDataAlignUserLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                     @Param("userIds") List<Long> userIds);

    void batchDeleteDataAlignUserCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                     @Param("userIds") List<Long> userIds);
}
