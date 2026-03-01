package com.taoxin.communitysharing.count.business.domain.databaseObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCountDo {
    private Long id;

    private Long userId;

    private Long fansTotal;

    private Long followingTotal;

    private Long contentTotal;

    private Long likeTotal;

    private Long collectTotal;
}