package com.taoxin.communitysharing.comment.business.controller;

import com.taoxin.communitysharing.comment.business.model.vo.req.CommentPublishReqVo;
import com.taoxin.communitysharing.comment.business.service.CommentServer;
import com.taoxin.communitysharing.common.response.Response;
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
    public Response<?> publishComment(@Validated @RequestBody CommentPublishReqVo commentPublishReqVo) {
        return commentServer.publishComtent(commentPublishReqVo);
    }
}
