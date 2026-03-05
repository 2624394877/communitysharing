package com.taoxin.communitysharing.user.business.controller;

import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.user.business.model.vo.req.FindUserInfoReqVo;
import com.taoxin.communitysharing.user.business.model.vo.req.UserUpdateMailReqVo;
import com.taoxin.communitysharing.user.business.model.vo.req.UserUpdatePhoneReqVo;
import com.taoxin.communitysharing.user.business.model.vo.res.FindUserInfoResVo;
import com.taoxin.communitysharing.user.business.model.vo.res.UserInfoResVo;
import com.taoxin.communitysharing.search.user.dto.requestDTO.*;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResponseDTO;
import com.taoxin.communitysharing.user.business.model.vo.UpdateUserInfoVO;
import com.taoxin.communitysharing.user.business.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Resource
    UserService userService;
    @PostMapping(value = "/update",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperationLog(description = "个人信息修改")
    public Response<?> Update(@Validated UpdateUserInfoVO updateUserInfoVO) {
        return userService.updateUserInfo(updateUserInfoVO);
    }

    @PostMapping(value = "/findUserById")
    @ApiOperationLog(description = "根据用户id查询用户")
    public Response<?> findUserById(@Validated @RequestBody FindUserByIdDTO findUserByIdDTO) {
        return userService.findUserById(findUserByIdDTO);
    }

    @GetMapping(value = "/findUser")
    @ApiOperationLog(description = "查询当前用户")
    public Response<FindUserByIdResDTO> findUser() {
        return userService.findUser();
    }

    @GetMapping(value = "/getUserInfo")
    @ApiOperationLog(description = "获取当前用户信息")
    public Response<UserInfoResVo> getUserInfo() {
        return userService.getUserInfo();
    }

    @PostMapping(value = "/updateUserMail")
    @ApiOperationLog(description = "修改用户邮箱")
    public Response<?> updateUserMail(@Validated @RequestBody UserUpdateMailReqVo userUpdateMailReqVo) {
        return userService.updateUserMail(userUpdateMailReqVo);
    }

    @PostMapping(value = "/updateUserPhone")
    @ApiOperationLog(description = "修改用户手机号")
    public Response<?> updateUserPhone(@Validated @RequestBody UserUpdatePhoneReqVo userUpdatePhoneReqVo) {
        return userService.updateUserPhone(userUpdatePhoneReqVo);
    }
    /* ----------------------- 服务间调用的接口 --------------------------- */
    @PostMapping(value = "/register")
    @ApiOperationLog(description = "用户注册")
    public Response<Long> register(@Validated @RequestBody RegisterUserDTO registerUserDTO) {
        return userService.registerUser(registerUserDTO);
    }

    @PostMapping(value = "/findUserByPhone")
    @ApiOperationLog(description = "根据手机号查询用户")
    public Response<?> selectByphone(@Validated @RequestBody FindUserByPhoneDTO findUserByPhoneDTO) {
        return userService.findUserByPhone(findUserByPhoneDTO);
    }

    @PostMapping(value = "/findUserByEmail")
    @ApiOperationLog(description = "根据邮箱查询用户")
    public Response<Long> selectByEmail(@Validated @RequestBody FindUserByEmailDTO findUserByEmailDTO) {
        return userService.findUserIdByEmail(findUserByEmailDTO);
    }

    @PostMapping(value = "/password/update")
    @ApiOperationLog(description = "修改密码")
    public Response<?> updatePassword(@Validated @RequestBody UpdateUserPasswordDTO updateUserPasswordDTO) {
        return userService.PasswordUpdate(updateUserPasswordDTO);
    }

    @PostMapping(value = "/findUsersById")
    @ApiOperationLog(description = "根据用户id查询用户列表")
    public Response<FindUsersByIdResponseDTO> findUsersById(@Validated @RequestBody FindUsersByIdDTO findUsersByIdDTO) {
        return userService.findUsersById(findUsersByIdDTO);
    }
}
