package com.taoxin.communitysharing.content.business.model.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeCollectStatusJudge {

    /**
     * 内容id
     */
    private Long contentId;
}
