package com.taoxin.communitysharing.search.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RebuildContentDocReqDTO {
    @NotNull(message = "contentId不能为空")
    private Long contentId;
}
