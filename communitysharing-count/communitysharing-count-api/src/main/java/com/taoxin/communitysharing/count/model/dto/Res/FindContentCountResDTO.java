package com.taoxin.communitysharing.count.model.dto.Res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindContentCountResDTO {

    private Long contentId;

    private Long likeTotal;

    private Long collectTotal;

    private Long commentTotal;
}
