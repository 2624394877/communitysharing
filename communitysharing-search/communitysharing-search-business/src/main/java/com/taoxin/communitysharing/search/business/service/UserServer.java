package com.taoxin.communitysharing.search.business.service;

import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.search.business.model.vo.req.SearchUserReqVo;
import com.taoxin.communitysharing.search.business.model.vo.res.SearchUserResVo;
import com.taoxin.communitysharing.search.dto.request.RebuildUserDocReqDTO;

public interface UserServer {
    /**
     * 搜索用户
     * @param reqVo 请求参数
     * @return 响应
     */
    PageResponse<SearchUserResVo> searchUser(SearchUserReqVo reqVo);

    /**
     * 重建用户文档
     * @param reqDTO 请求参数
     * @return 响应
     */
    Response<Long> rebuildDocument(RebuildUserDocReqDTO reqDTO);
}
