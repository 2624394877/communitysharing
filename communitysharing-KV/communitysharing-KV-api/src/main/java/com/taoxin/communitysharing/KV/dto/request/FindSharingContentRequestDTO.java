package com.taoxin.communitysharing.KV.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindSharingContentRequestDTO {
    @NotNull(message = "uuid不能为空")
    private String uuid;
}
