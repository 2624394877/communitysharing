package com.taoxin.communitysharing.content.business.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentPublishListResVo {

    private List<ContentPublishListItemResVo> contents;

    /**
     * 下一页游标
     */
    private Long nextCursor;
}
