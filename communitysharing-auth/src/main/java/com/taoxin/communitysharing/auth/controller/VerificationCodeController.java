package com.taoxin.communitysharing.auth.controller;

import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.auth.model.vo.verificationcode.SendVerificationCodeReqVo;
import com.taoxin.communitysharing.auth.model.vo.verificationcode.SendVerificationCodeReqVoSMTP;
import com.taoxin.communitysharing.auth.service.VerificationCodeService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationCodeController {

    @Resource
    private VerificationCodeService verificationCodeService;

    @PostMapping("/sendVerificationCode")
    @ApiOperationLog(description = "发送手机号验证码")
    public Response<?> sendVerificationCode(@RequestBody @Validated SendVerificationCodeReqVo sendVerificationCodeReqVo) {
        return verificationCodeService.sendVerificationCode(sendVerificationCodeReqVo);
    }

    @PostMapping("/sendVerificationCodeByEmail")
    @ApiOperationLog(description = "发送邮箱验证码")
    public Response<?> sendVerificationCode2(@RequestBody @Validated SendVerificationCodeReqVoSMTP sendVerificationCodeReqVoSMTP) {
        return verificationCodeService.sendVerificationCode(sendVerificationCodeReqVoSMTP);
    }
}
