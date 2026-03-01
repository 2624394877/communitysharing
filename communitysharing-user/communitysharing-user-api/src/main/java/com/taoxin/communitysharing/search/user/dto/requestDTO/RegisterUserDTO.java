package com.taoxin.communitysharing.search.user.dto.requestDTO;

import com.taoxin.communitysharing.common.validator.phone.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterUserDTO {
    @NotBlank(message = "手机号不能为空")
    @PhoneNumber
    private String phone;
}
