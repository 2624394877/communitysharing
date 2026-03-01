package com.taoxin.communitysharing.search.user.api;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.search.user.constant.ApiConstant;
import com.taoxin.communitysharing.search.user.dto.requestDTO.*;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByPhoneResDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstant.SERVICE_NAME)
public interface UserFeignApi {
    String PREFIX = "/user";

    @PostMapping(value = PREFIX + "/register")
    Response<Long> register(@RequestBody RegisterUserDTO registerUserDTO);

    @PostMapping(value = PREFIX + "/findUserByPhone")
    Response<FindUserByPhoneResDTO> findUserByPhone(@RequestBody FindUserByPhoneDTO findUserByPhoneDTO);

    @PostMapping(value = PREFIX + "/findUserByEmail")
    Response<Long> findUserByEmail(@RequestBody FindUserByEmailDTO findUserByEmailDTO);

    @PostMapping(value = PREFIX + "/password/update")
    Response<?> updatePassword(@RequestBody UpdateUserPasswordDTO updateUserPasswordDTO);

    @PostMapping(value = PREFIX + "/findUserById")
    Response<FindUserByIdResDTO> findUserById(@RequestBody FindUserByIdDTO findUserByIdDTO);

    @PostMapping(value = PREFIX + "/findUsersById")
    Response<FindUsersByIdResponseDTO> findUsersById(@RequestBody FindUsersByIdDTO findUsersByIdDTO);
}
