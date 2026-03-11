package com.taoxin.communitysharing.search.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RebuildUserDocReqDTO {
    @NotNull(message = "userId不能为空")
    private String userId;
}
