package com.taoxin.communitysharing.user.relation.buiness.rpc;

import cn.hutool.core.collection.CollUtil;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.search.user.api.UserFeignApi;
import com.taoxin.communitysharing.search.user.dto.requestDTO.FindUserByIdDTO;
import com.taoxin.communitysharing.search.user.dto.requestDTO.FindUsersByIdDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResponseDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
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

    public FindUsersByIdResponseDTO getUsersInfoByIds(List<Long> userIds) {
        FindUsersByIdDTO findUsersByIdDTO = new FindUsersByIdDTO();
        findUsersByIdDTO.setUsersId(userIds);
        Response<FindUsersByIdResponseDTO> response = userFeignApi.findUsersById(findUsersByIdDTO);
        log.info("getUsersInfoByIds response: {}", response.getData().getUsersInfo());
        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData().getUsersInfo())) {
            return null;
        }
        return response.getData();
    }
}
