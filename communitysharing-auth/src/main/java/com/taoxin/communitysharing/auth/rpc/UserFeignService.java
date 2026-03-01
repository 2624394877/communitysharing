package com.taoxin.communitysharing.auth.rpc;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.search.user.api.UserFeignApi;
import com.taoxin.communitysharing.search.user.dto.requestDTO.FindUserByEmailDTO;
import com.taoxin.communitysharing.search.user.dto.requestDTO.FindUserByPhoneDTO;
import com.taoxin.communitysharing.search.user.dto.requestDTO.RegisterUserDTO;
import com.taoxin.communitysharing.search.user.dto.requestDTO.UpdateUserPasswordDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByPhoneResDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 用户服务Feign服务
 * @author taoxin
 * @date 2026/1/10
 * @description: 用户服务Feign服务
 */
@Service
public class UserFeignService {
    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 注册用户
     * @param phone
     * @return 用户id
     */
    public Long register(String phone) {
        RegisterUserDTO registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setPhone(phone); // 设置手机号

        Response<Long> response = userFeignApi.register(registerUserDTO); // 调用Feign接口
        if (!response.isSuccess()) {
            return null;
        }
        return response.getData();
    }

    /**
     * 通过手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    public FindUserByPhoneResDTO findUserByPhone(String phone) {
        FindUserByPhoneDTO findUserByPhoneDTO = new FindUserByPhoneDTO();
        findUserByPhoneDTO.setPhone(phone);
        Response<FindUserByPhoneResDTO> response = userFeignApi.findUserByPhone(findUserByPhoneDTO); // 调用Feign接口
        if (!response.isSuccess()) {
            return null;
        }
        return response.getData();
    }

    /**
     * 通过邮箱查询用户
     * @param email 邮箱
     * @return 用户id
     */
    public Long findUserByEmail(String email) {
        FindUserByEmailDTO findUserByEmailDTO = new FindUserByEmailDTO();
        findUserByEmailDTO.setEmail(email);
        Response<Long> response = userFeignApi.findUserByEmail(findUserByEmailDTO);
        if (!response.isSuccess()) {
            return null;
        }
        return response.getData();
    }

    /**
     * 修改用户密码
     * @param EncodedPassword 密码
     * @return 修改结果
     */
    public Response<?> updateUserPassword(String EncodedPassword) {
        UpdateUserPasswordDTO updateUserPasswordDTO = new UpdateUserPasswordDTO(EncodedPassword);
        updateUserPasswordDTO.setEncodePassword(EncodedPassword);
        return userFeignApi.updatePassword(updateUserPasswordDTO);
    }
}
