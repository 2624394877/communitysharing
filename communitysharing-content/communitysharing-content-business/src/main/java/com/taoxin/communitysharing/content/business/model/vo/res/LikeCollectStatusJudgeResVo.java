package com.taoxin.communitysharing.content.business.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeCollectStatusJudgeResVo {
    private Long contentId;

    private boolean like;

    private boolean collect;
}
