package com.taoxin.communitysharing.KV.service;

import com.taoxin.communitysharing.KV.domian.dataobject.CommentContentPrimaryKey;
import com.taoxin.communitysharing.KV.dto.request.AddBatchCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.request.BatchFindCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.request.DeleteCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.response.FindCommentContentRspDTO;
import com.taoxin.communitysharing.common.response.Response;

import java.util.List;

public interface CommentContentService {
    /**
     * 批量添加评论内容
     * @param addBatchCommentContentReqDTO 评论列表请求参数
     * @return 响应结果
     */
    Response<?> addBatchCommentContent(AddBatchCommentContentReqDTO addBatchCommentContentReqDTO);

    /**
     * 批量查询评论内容
     * @param batchFindCommentContentReqDTO 批量查询笔记内容请求参数
     */
    Response<List<FindCommentContentRspDTO>> findBatchCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO);

    /**
     * 批量删除评论内容
     * @param commentContentPrimaryKey 批量删除笔记内容请求参数
     * @return 响应结果
     */
    Response<?> deleteBatchCommentContent(DeleteCommentContentReqDTO commentContentPrimaryKey);
}
