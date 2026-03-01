package com.taoxin.communitysharing.count.business.domain.databaseObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentCountDo {
    private Long id;

    private Long contentId;

    private Long likeTotal;

    private Long collectTotal;

    private Long commentTotal;
}