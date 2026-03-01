package com.taoxin.communitysharing.search.business.domain.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SelectMapper {
    /**
     * 查询ES索引数据
     * @param contentId 目标id
     * @return 查询结果
     */
    List<Map<String, Object>> selectEsContentIndexData(@Param("contentId") Long contentId, @Param("userId") Long userId);

    /**
     * 查询ES索引数据
     * @param userId 目标id
     * @return 索引数据
     */
    List<Map<String, Object>> selectEsUserIndexData(@Param("userId") Long userId);
}
