package com.taoxin.communitysharing.search.user.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserPasswordDTO {
    @NotBlank(message = "密码不能为空")
    private String EncodePassword;
}
