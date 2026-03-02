package com.taoxin.communitysharing.comment.business.rpc;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.search.user.api.UserFeignApi;
import com.taoxin.communitysharing.search.user.dto.requestDTO.FindUsersByIdDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResponseDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserFeignApiService {
    @Resource
    private UserFeignApi userFeignApi;

    public List<FindUsersByIdResDTO> findUsersById(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) return null;

        FindUsersByIdDTO findUsersByIdDTO = FindUsersByIdDTO.builder()
                .usersId(new ArrayList<>(new LinkedHashSet<>(userIds)))
                .build();
        Response<FindUsersByIdResponseDTO> response = userFeignApi.findUsersById(findUsersByIdDTO);

        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData().getUsersInfo()))
            return null;
        return response.getData().getUsersInfo();
    }
}
