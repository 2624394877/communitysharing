package com.taoxin.communitysharing.count.business.domain.databaseObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentCollectionDo {
    private Long id;

    private Long userId;

    private Long contentId;

    private LocalDateTime createTime;

    private Integer status;
}