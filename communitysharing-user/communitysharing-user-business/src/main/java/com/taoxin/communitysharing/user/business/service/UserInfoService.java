package com.taoxin.communitysharing.user.business.service;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.user.business.model.vo.req.FindUserInfoReqVo;
import com.taoxin.communitysharing.user.business.model.vo.res.FindUserInfoResVo;

public interface UserInfoService {
    Response<FindUserInfoResVo> findUserInfo(FindUserInfoReqVo findUserInfoReqVo);
}
