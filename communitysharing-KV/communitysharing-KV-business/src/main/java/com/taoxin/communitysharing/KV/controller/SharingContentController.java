package com.taoxin.communitysharing.KV.controller;

import com.taoxin.communitysharing.KV.dto.request.AddBatchCommentContentReqDTO;
import com.taoxin.communitysharing.KV.service.CommentContentService;
import com.taoxin.communitysharing.KV.service.SharingContent;
import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.KV.dto.request.AddSharingContentRequestDTO;
import com.taoxin.communitysharing.KV.dto.request.DeleteSharingContentRequestDTO;
import com.taoxin.communitysharing.KV.dto.request.FindSharingContentRequestDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/kv")
public class SharingContentController {
    @Resource
    private SharingContent sharingContent;
    @Resource
    private CommentContentService commentContentService;

    @PostMapping("/sharing/content/add")
    @ApiOperationLog(description = "添加笔记内容")
    public Response<?> addSharingContent(@RequestBody AddSharingContentRequestDTO requestDTO) {
        return sharingContent.addSharingContent(requestDTO);
    }

    @PostMapping("/sharing/content/find")
    @ApiOperationLog(description = "查询笔记内容")
    public Response<?> findSharingContent(@RequestBody FindSharingContentRequestDTO requestDTO) {
        return sharingContent.getSharingContent(requestDTO);
    }

    @PostMapping("/sharing/content/delete")
    @ApiOperationLog(description = "删除笔记内容")
    public Response<?> deleteSharingContent(@RequestBody DeleteSharingContentRequestDTO requestDTO) {
        return sharingContent.deleteSharingContent(requestDTO);
    }

    @PostMapping("/sharing/content/addBatchComment")
    @ApiOperationLog(description = "批量添加笔记内容")
    public Response<?> addBatchComment(@Validated @RequestBody AddBatchCommentContentReqDTO requestDTO) {
        return commentContentService.addBatchCommentContent(requestDTO);
    }
}
