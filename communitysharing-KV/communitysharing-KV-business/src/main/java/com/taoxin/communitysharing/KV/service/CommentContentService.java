package com.taoxin.communitysharing.KV.service;

import com.taoxin.communitysharing.KV.dto.request.AddBatchCommentContentReqDTO;
import com.taoxin.communitysharing.common.response.Response;

public interface CommentContentService {
    /**
     * 批量添加笔记内容
     * @param addBatchCommentContentReqDTO 评论列表请求参数
     * @return 响应结果
     */
    Response<?> addBatchCommentContent(AddBatchCommentContentReqDTO addBatchCommentContentReqDTO);
}
