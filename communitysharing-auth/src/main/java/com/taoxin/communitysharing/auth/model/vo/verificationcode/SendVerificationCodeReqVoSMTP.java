package com.taoxin.communitysharing.auth.model.vo.verificationcode;

import com.taoxin.communitysharing.common.validator.mail.MailParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendVerificationCodeReqVoSMTP {
    @NotBlank(message = "邮箱不能为空")
    @MailParams
    private String email;
}
