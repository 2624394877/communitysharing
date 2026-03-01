package com.taoxin.communitysharing.content.business.rpc;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.search.user.api.UserFeignApi;
import com.taoxin.communitysharing.search.user.dto.requestDTO.FindUserByIdDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserFeignApiService {
    @Resource
    private UserFeignApi userFeignApi;

    public FindUserByIdResDTO getUserInfoById(Long userId) {
        FindUserByIdDTO findUserByIdDTO = new FindUserByIdDTO();
        findUserByIdDTO.setUserId(userId);
        Response<FindUserByIdResDTO> response = userFeignApi.findUserById(findUserByIdDTO);
        if (response == null || !response.isSuccess()) {
            return null;
        }
        return response.getData();
    }
}
