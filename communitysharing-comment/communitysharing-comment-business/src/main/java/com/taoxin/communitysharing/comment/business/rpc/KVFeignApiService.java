package com.taoxin.communitysharing.comment.business.rpc;

import com.google.common.collect.Lists;
import com.taoxin.communitysharing.KV.api.KVFeignApi;
import com.taoxin.communitysharing.KV.dto.request.AddBatchCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.request.CommentContentReqDTO;
import com.taoxin.communitysharing.comment.business.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.comment.business.model.bo.CommentBo;
import com.taoxin.communitysharing.common.constant.DateConstants;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.common.uitl.DateUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class KVFeignApiService {
    @Resource
    private KVFeignApi kvFeignApi;

    public boolean batchSaveCommentContent(List<CommentBo> commentBoList) {
        List<CommentContentReqDTO> commentContentReqDTOS = Lists.newArrayList();
        // BO 转 DTO
        commentBoList.forEach(commentBo -> {
            CommentContentReqDTO commentContentReqDTO = CommentContentReqDTO.builder()
                    .contentId(commentBo.getContentId())
                    .yearMonth(commentBo.getCreateTime().format(DateTimeFormatter.ofPattern(DateConstants.YEAR_MONTH_PATTERN)))
                    .commentId(commentBo.getContentUuid())
                    .comment(commentBo.getContent())
                    .build();
            commentContentReqDTOS.add(commentContentReqDTO);
        });
        AddBatchCommentContentReqDTO addBatchCommentContentReqDTO = AddBatchCommentContentReqDTO.builder()
                .comments(commentContentReqDTOS)
                .build();
        Response<?> response = kvFeignApi.addBatchComment(addBatchCommentContentReqDTO);

        if (!response.isSuccess()) {
            log.error("【保存笔记内容失败】数据：{}", commentContentReqDTOS);
            throw new BusinessException(ResponseStatusEnum.SYSTEMP_ERROR.getErrorCode(),"保存笔记内容失败");
        }
        return true;
    }
}
