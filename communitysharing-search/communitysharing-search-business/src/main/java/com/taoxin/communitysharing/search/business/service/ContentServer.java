package com.taoxin.communitysharing.search.business.service;

import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.search.business.model.vo.req.SearchContentReqVo;
import com.taoxin.communitysharing.search.business.model.vo.res.SearchContentResVo;
import com.taoxin.communitysharing.search.dto.request.RebuildContentDocReqDTO;

public interface ContentServer {
    /**
     * 搜索内容
     * @param reqVo 请求参数
     * @return 响应
     */
    PageResponse<SearchContentResVo> searchContent(SearchContentReqVo reqVo);

    /**
     * 重建内容文档
     * @param reqDTO 请求参数
     * @return 响应
     */
    Response<Long> rebuildDocument(RebuildContentDocReqDTO reqDTO);
}
