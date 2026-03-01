package com.taoxin.communitysharing.auth.controller;

import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.auth.model.vo.user.UserLoginRequestVO;
import com.taoxin.communitysharing.auth.model.vo.user.UserPasswordReqVo;
import com.taoxin.communitysharing.auth.model.vo.user.UserloginRequestByEmailVo;
import com.taoxin.communitysharing.auth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class AuthController {
    @Resource
    private UserService userService; // 用户服务

    @PostMapping("/login")
    @ApiOperationLog(description = "用户登录/注册")
    public Response<?> login(@RequestBody @Validated UserLoginRequestVO userLoginRequestVO) {
        return userService.LoginAndRegister(userLoginRequestVO);
    }

    @PostMapping("/loginByEmail")
    @ApiOperationLog(description = "用户邮箱登录")
    public Response<?> loginByEmail(@RequestBody @Validated UserloginRequestByEmailVo userloginRequestByEmailVo) {
        return userService.LoginByEmail(userloginRequestByEmailVo);
    }

    @GetMapping("/loginout")
    @ApiOperationLog(description = "用户登出")
    public Response<?> loginout() {
        return userService.outLogin();
    }

    @PostMapping("/password/update")
    @ApiOperationLog(description = "修改密码")
    public Response<?> updatePassword(@RequestBody @Validated UserPasswordReqVo userPasswordReqVo) {
        return userService.PasswordUpdate(userPasswordReqVo);
    }
}
