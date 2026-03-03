package com.taoxin.communitysharing.KV.controller;

import com.taoxin.communitysharing.KV.domian.dataobject.CommentContentPrimaryKey;
import com.taoxin.communitysharing.KV.dto.request.*;
import com.taoxin.communitysharing.KV.dto.response.FindCommentContentRspDTO;
import com.taoxin.communitysharing.KV.service.CommentContentService;
import com.taoxin.communitysharing.KV.service.SharingContent;
import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import com.taoxin.communitysharing.common.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @PostMapping("/sharing/content/batch/query")
    @ApiOperationLog(description = "批量查询批量内容")
    public Response<List<FindCommentContentRspDTO>> batchQuery(@Validated @RequestBody BatchFindCommentContentReqDTO requestDTO) {
        return commentContentService.findBatchCommentContent(requestDTO);
    }

    @PostMapping("comment/content/delete")
    @ApiOperationLog(description = "删除评论内容")
    public Response<?> batchDelete(@RequestBody DeleteCommentContentReqDTO commentContentPrimaryKey) {
        return commentContentService.deleteBatchCommentContent(commentContentPrimaryKey);
    }
}
