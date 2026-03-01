package com.taoxin.communitysharing.KV.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddSharingContentRequestDTO {
    @NotNull(message = "uuid不能为空")
    private String uuid;
    @NotBlank(message = "content不能为空")
    private String content;
}
