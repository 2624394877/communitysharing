package com.taoxin.communitysharing.KV.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteSharingContentRequestDTO {
    @NotBlank(message = "uuid不能为空")
    private String uuid;
}
