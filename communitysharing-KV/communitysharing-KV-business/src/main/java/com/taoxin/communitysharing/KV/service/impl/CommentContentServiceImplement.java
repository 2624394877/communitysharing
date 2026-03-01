package com.taoxin.communitysharing.KV.service.impl;

import com.taoxin.communitysharing.KV.domian.dataobject.CommentContentDO;
import com.taoxin.communitysharing.KV.domian.dataobject.CommentContentPrimaryKey;
import com.taoxin.communitysharing.KV.dto.request.AddBatchCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.request.CommentContentReqDTO;
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
}
