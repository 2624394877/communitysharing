package com.taoxin.communitysharing.comment.business.domain.databaseObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentLikeDo {
    private Long id;

    private Long userId;

    private Long commentId;

    private LocalDateTime createTime;
}