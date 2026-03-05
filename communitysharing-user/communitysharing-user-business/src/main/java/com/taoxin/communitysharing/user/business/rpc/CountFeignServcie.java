package com.taoxin.communitysharing.user.business.rpc;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.count.api.CountFeignServer;
import com.taoxin.communitysharing.count.model.vo.req.FindUserCountsByIdReqVo;
import com.taoxin.communitysharing.count.model.vo.res.FindUserCountsByIdResVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class CountFeignServcie {
    @Resource
    private CountFeignServer countFeignApi;

    public FindUserCountsByIdResVo findUserCountsById(Long userId) {
        Response<FindUserCountsByIdResVo> response = countFeignApi.findUserCountsById(FindUserCountsByIdReqVo.builder().userId(userId).build());
        if (Objects.isNull(response) || !response.isSuccess()) return null;
        return response.getData();
    }
}
