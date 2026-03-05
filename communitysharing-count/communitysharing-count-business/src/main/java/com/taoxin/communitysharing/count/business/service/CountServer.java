package com.taoxin.communitysharing.count.business.service;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.count.business.model.vo.req.FindUserCountsByIdReqVo;
import com.taoxin.communitysharing.count.business.model.vo.res.FindUserCountsByIdResVo;
import com.taoxin.communitysharing.count.model.dto.Req.FindContentCountReqDTO;
import com.taoxin.communitysharing.count.model.dto.Res.FindContentCountResDTO;

import java.util.List;

public interface CountServer {

    Response<FindUserCountsByIdResVo> findUserCountsById(FindUserCountsByIdReqVo reqVo);

    Response<List<FindContentCountResDTO>> findContentCount(FindContentCountReqDTO reqVo);
}
