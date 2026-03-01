package com.taoxin.communitysharing.auth.service;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.auth.model.vo.user.UserLoginRequestVO;
import com.taoxin.communitysharing.auth.model.vo.user.UserPasswordReqVo;
import com.taoxin.communitysharing.auth.model.vo.user.UserloginRequestByEmailVo;

public interface UserService {

    Response<?> PasswordUpdate(UserPasswordReqVo userPasswordReqVo);

    /**
     * 退出登录
     * @return
     */
    Response<?> outLogin();

    /**
     * 登录和注册
     * @param userLoginRequestVO 登录和注册请求参数
     * @return 响应对象
     */
    Response<?> LoginAndRegister(UserLoginRequestVO userLoginRequestVO);

    /**
     * 邮箱登录
     * @param userloginRequestByEmailVo 邮箱登录请求参数
     * @return 响应对象
     */
    Response<?> LoginByEmail(UserloginRequestByEmailVo userloginRequestByEmailVo);
}
