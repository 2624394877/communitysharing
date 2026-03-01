package com.taoxin.communitysharing.comment.business.service;

import com.taoxin.communitysharing.comment.business.model.vo.req.CommentPublishReqVo;
import com.taoxin.communitysharing.common.response.Response;

public interface CommentServer {
    Response<?> publishComtent(CommentPublishReqVo commentPublishReqVo);
}
