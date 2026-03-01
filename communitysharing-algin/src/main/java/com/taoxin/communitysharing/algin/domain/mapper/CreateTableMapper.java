package com.taoxin.communitysharing.algin.domain.mapper;

public interface CreateTableMapper {

    /**
     * 动态创建日增表 关注
     * @param tableNameSuffix 表名后缀
     */
    void createDataAlignFollowingCountTempTable(String tableNameSuffix);

    /**
     * 动态创建日增表 粉丝
     * @param tableNameSuffix 表名后缀
     */
    void createDataAlignFansCountTempTable(String tableNameSuffix);

    /**
     * 动态创建日增表 收藏
     * @param tableNameSuffix 表名后缀
     */
    void createDataAlignCollectCountTemTable(String tableNameSuffix);

    /**
     * 动态创建日增表 点赞
     * @param tableNameSuffix 表名后缀
     */
    void createDataAlignLikeCountTemTable(String tableNameSuffix);

    /**
     * 动态创建日增表 用户获得收藏
     * @param tableNameSuffix 表名后缀
     */
    void createDataAlignUserCollectCountTemTable(String tableNameSuffix);

    /**
     * 动态创建日增表 用户获得点赞
     * @param tableNameSuffix 表名后缀
     */
    void createDataAlignUserLikeCountTemTable(String tableNameSuffix);

    /**
     * 动态创建日增表 用户发布数
     * @param tableNameSuffix 表名后缀
     */
    void createDataAlignUserPublishCountTemTable(String tableNameSuffix);
}
