package com.taoxin.communitysharing.user.business.controller;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import com.taoxin.communitysharing.user.business.model.vo.req.FindUserInfoReqVo;
import com.taoxin.communitysharing.user.business.model.vo.res.FindUserInfoResVo;
import com.taoxin.communitysharing.user.business.service.UserInfoService;
import com.taoxin.communitysharing.user.business.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class UserInfoController {
    @Resource
    UserInfoService userService;

    @PostMapping(value = "/info/getUserInfo")
    @ApiOperationLog(description = "获取用户信息")
    public Response<FindUserInfoResVo> getUserInfo(@Validated @RequestBody FindUserInfoReqVo findUserInfoReqVo) {
        return userService.findUserInfo(findUserInfoReqVo);
    }
}
