package com.taoxin.communitysharing.count.business.service;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.count.business.model.vo.req.FindUserCountsByIdReqVo;
import com.taoxin.communitysharing.count.business.model.vo.res.FindUserCountsByIdResVo;

public interface CountServer {

    Response<FindUserCountsByIdResVo> findUserCountsById(FindUserCountsByIdReqVo reqVo);
}
