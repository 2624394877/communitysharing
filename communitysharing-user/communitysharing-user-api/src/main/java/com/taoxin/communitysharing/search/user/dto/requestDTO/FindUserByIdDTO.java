package com.taoxin.communitysharing.search.user.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserByIdDTO {
    @NotNull(message = "用户id不能为空")
    private Long userId;
}
