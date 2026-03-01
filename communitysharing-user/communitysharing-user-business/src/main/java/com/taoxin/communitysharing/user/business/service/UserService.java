package com.taoxin.communitysharing.user.business.service;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.user.business.model.vo.req.UserUpdateMailReqVo;
import com.taoxin.communitysharing.user.business.model.vo.req.UserUpdatePhoneReqVo;
import com.taoxin.communitysharing.user.business.model.vo.res.UserInfoResVo;
import com.taoxin.communitysharing.search.user.dto.requestDTO.*;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResponseDTO;
import com.taoxin.communitysharing.user.business.model.vo.UpdateUserInfoVO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByPhoneResDTO;


public interface UserService {
    Response<?> updateUserInfo(UpdateUserInfoVO updateUserInfoVO);

    Response<Long> registerUser(RegisterUserDTO registerUserDTO);

    Response<FindUserByPhoneResDTO> findUserByPhone(FindUserByPhoneDTO findUserByPhoneDTO);

    Response<Long> findUserIdByEmail(FindUserByEmailDTO findUserByEmailDTO);

    Response<?> PasswordUpdate(UpdateUserPasswordDTO updateUserPasswordDTO);

    Response<?> findUserById(FindUserByIdDTO findUserByIdDTO);

    Response<FindUsersByIdResponseDTO> findUsersById(FindUsersByIdDTO findUsersByIdDTO);

    Response<FindUserByIdResDTO> findUser();

    Response<UserInfoResVo> getUserInfo();

    Response<?> updateUserMail(UserUpdateMailReqVo userUpdateMailReqVo);

    Response<?> updateUserPhone(UserUpdatePhoneReqVo userUpdatePhoneReqVo);
}
