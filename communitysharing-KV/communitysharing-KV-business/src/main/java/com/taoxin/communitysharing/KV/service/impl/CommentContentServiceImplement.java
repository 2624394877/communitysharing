package com.taoxin.communitysharing.KV.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.KV.domian.dataobject.CommentContentDO;
import com.taoxin.communitysharing.KV.domian.dataobject.CommentContentPrimaryKey;
import com.taoxin.communitysharing.KV.domian.repository.CommentContentRepository;
import com.taoxin.communitysharing.KV.dto.request.AddBatchCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.request.BatchFindCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.request.CommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.request.FindCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.response.FindCommentContentRspDTO;
import com.taoxin.communitysharing.KV.service.CommentContentService;
import com.taoxin.communitysharing.common.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CommentContentServiceImplement implements CommentContentService {
    @Resource
    private CassandraTemplate cassandraTemplate;
    @Resource
    private CommentContentRepository commentContentRepository;
    @Override
    public Response<?> addBatchCommentContent(AddBatchCommentContentReqDTO addBatchCommentContentReqDTO) {
        List<CommentContentReqDTO> comments = addBatchCommentContentReqDTO.getComments();
        List<CommentContentDO> contentDOS = comments.stream()
                .map(CommentContentReqDTO -> {
                    CommentContentPrimaryKey commentContentPrimaryKey = CommentContentPrimaryKey.builder()
                            .contentId(CommentContentReqDTO.getContentId())
                            .yearMonth(CommentContentReqDTO.getYearMonth())
                            .commentId(UUID.fromString(CommentContentReqDTO.getCommentId()))
                            .build();
                    CommentContentDO commentContentDO = CommentContentDO.builder()
                            .commentContentPrimaryKey(commentContentPrimaryKey)
                            .comment(CommentContentReqDTO.getComment())
                            .build();
                    return commentContentDO;
                }).toList();

        // 批量插入
        cassandraTemplate.batchOps()
                .insert(contentDOS)
                .execute();
        return Response.success();
    }

    @Override
    public Response<List<FindCommentContentRspDTO>> findBatchCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO) {
        long contentId = batchFindCommentContentReqDTO.getContentId(); // 评论所属内容的id
        List<FindCommentContentReqDTO> commentContentKeys = batchFindCommentContentReqDTO.getCommentContentKeys();
        // 年月
        List<String> yearMonths = commentContentKeys.stream()
                .map(FindCommentContentReqDTO::getYearMonth)
                .distinct()
                .toList();
        // 评论uuid
        List<UUID> commentIds = commentContentKeys.stream()
                .map(FindCommentContentReqDTO-> UUID.fromString(FindCommentContentReqDTO.getCommentId()))
                .distinct()
                .toList();
        // 批量查询
        List<CommentContentDO> commentContentDoS = commentContentRepository.findComment(contentId, yearMonths, commentIds);
        log.info("【批量查询批量内容】查询结果: {}", commentContentDoS);
        // DO转DTO
        List<FindCommentContentRspDTO> findCommentContentRspDTOS = Lists.newArrayList();
        if (CollUtil.isNotEmpty(commentContentDoS)) {
            findCommentContentRspDTOS = commentContentDoS.stream()
                    .map(commentContentDO -> FindCommentContentRspDTO.builder()
                            .commentId(String.valueOf(commentContentDO.getCommentContentPrimaryKey().getCommentId()))
                            .comment(commentContentDO.getComment())
                            .build()).toList();
        }
        return Response.success(findCommentContentRspDTOS);
    }
}
