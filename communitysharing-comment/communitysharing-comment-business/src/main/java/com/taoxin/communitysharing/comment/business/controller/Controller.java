package com.taoxin.communitysharing.comment.business.controller;

import com.taoxin.communitysharing.comment.business.model.vo.req.CommentPublishReqVo;
import com.taoxin.communitysharing.comment.business.model.vo.req.FindCommentPageListReqVo;
import com.taoxin.communitysharing.comment.business.model.vo.res.FindCommentItemRspVo;
import com.taoxin.communitysharing.comment.business.service.CommentServer;
import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class Controller {
    @Resource
    private CommentServer commentServer;

    @PostMapping("/publish")
    @ApiOperationLog(description = "发布评论")
    public Response<?> publishComment(@Validated @RequestBody CommentPublishReqVo commentPublishReqVo) {
        return commentServer.publishComtent(commentPublishReqVo);
    }

    @PostMapping("/batch/query")
    @ApiOperationLog(description = "批量查询")
    public PageResponse<FindCommentItemRspVo> findCommentPageList(@Validated @RequestBody FindCommentPageListReqVo findCommentPageListReqVo) {
        return commentServer.findCommentPageList(findCommentPageListReqVo);
    }
}
