package com.taoxin.communitysharing.user.relation.buiness.domain.databaseObject;

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
public class FansDo {
    private Long id;

    private Long userId;

    private Long fansUserId;

    private LocalDateTime createTime;
}