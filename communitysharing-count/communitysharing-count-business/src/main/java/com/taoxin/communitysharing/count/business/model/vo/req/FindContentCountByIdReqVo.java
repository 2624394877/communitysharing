package com.taoxin.communitysharing.count.business.model.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindContentCountByIdReqVo {
    private String contentId;
}
