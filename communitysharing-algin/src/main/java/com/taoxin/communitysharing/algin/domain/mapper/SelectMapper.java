package com.taoxin.communitysharing.algin.domain.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SelectMapper {
    /**
     * 日增量表：关注数计数变更 - 批量查询
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignFollowingCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                               @Param("batchSize") int batchSize);

    /**
     * 查询 t_following 关注表，获取关注总数
     * @param userId
     * @return
     */
    int selectCountFromFollowingTableByUserId(long userId);

    /**
     * 日增量表：点赞数计数变更 - 批量查询
     */
    List<Long> selectBatchFromDataAlignLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                               @Param("batchSize") int batchSize);
    int selectCountFromLikeTableByContentId(long contentId);

    List<Long> selectBatchFromDataAlignCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                          @Param("batchSize") int batchSize);
    int selectCountFromCollectTableByContentId(long contentId);

    List<Long> selectBatchFromDataAlignPublishCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                          @Param("batchSize") int batchSize);
    int selectCountFromPublishTableByCreatorId(long creatorId);

    List<Long> selectBatchFromDataAlignFansCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                          @Param("batchSize") int batchSize);
    int selectCountFromFansTableByUserId(long userId);

    List<Long> selectBatchFromDataAlignUserLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                          @Param("batchSize") int batchSize);
    int selectCountFromUserLikeTableByUserId(long userId);

    List<Long> selectBatchFromDataAlignUserCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                          @Param("batchSize") int batchSize);
    int selectCountFromUserCollectTableByUserId(long userId);
}
