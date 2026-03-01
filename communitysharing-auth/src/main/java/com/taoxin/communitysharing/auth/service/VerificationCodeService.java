package com.taoxin.communitysharing.auth.service;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.auth.model.vo.verificationcode.SendVerificationCodeReqVo;
import com.taoxin.communitysharing.auth.model.vo.verificationcode.SendVerificationCodeReqVoSMTP;

public interface VerificationCodeService {
    /**
     * 发送验证码
     * @param sendVerificationCodeReqVo 发送验证码请求参数
     * @return 响应对象
     */
    Response<?> sendVerificationCode(SendVerificationCodeReqVo sendVerificationCodeReqVo);


    /**
     * 邮件发送验证码
     * @param sendVerificationCodeReqVoSMTP 邮件发送验证码请求参数
     * @return 响应对象
     */
    Response<?> sendVerificationCode(SendVerificationCodeReqVoSMTP sendVerificationCodeReqVoSMTP);
}
