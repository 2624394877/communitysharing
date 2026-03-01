package com.taoxin.communitysharing.count.business.domain.databaseObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentLikeDo {
    private Long id;

    private Long userId;

    private Long contentId;

    private LocalDateTime createTime;

    private Integer status;
}