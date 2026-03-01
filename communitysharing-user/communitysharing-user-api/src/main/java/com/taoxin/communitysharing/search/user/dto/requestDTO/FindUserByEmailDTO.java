package com.taoxin.communitysharing.search.user.dto.requestDTO;

import com.taoxin.communitysharing.common.validator.mail.MailParams;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserByEmailDTO {
    @NotBlank(message = "邮箱不能为空")
    @MailParams
    private String email;
}
