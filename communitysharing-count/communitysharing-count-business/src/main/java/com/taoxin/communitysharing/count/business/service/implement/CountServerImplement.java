package com.taoxin.communitysharing.count.business.service.implement;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.count.business.model.vo.req.FindUserCountsByIdReqVo;
import com.taoxin.communitysharing.count.business.model.vo.res.FindUserCountsByIdResVo;
import com.taoxin.communitysharing.count.business.service.CountServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CountServerImplement implements CountServer {
    @Override
    public Response<FindUserCountsByIdResVo> findUserCountsById(FindUserCountsByIdReqVo reqVo) {
        Long userId = reqVo.getUserId();
        // todo redis中查询
        return null;
    }
}
