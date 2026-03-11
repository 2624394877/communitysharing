package com.taoxin.communitysharing.count.business.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindContentCountByIdResVo {
    private String contentId;

    private Long likeTotal;

    private Long collectTotal;

    private Long commentTotal;
}
