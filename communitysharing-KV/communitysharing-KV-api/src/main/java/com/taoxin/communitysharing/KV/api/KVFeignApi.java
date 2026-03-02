package com.taoxin.communitysharing.KV.api;

import com.taoxin.communitysharing.KV.dto.request.*;
import com.taoxin.communitysharing.KV.dto.response.FindCommentContentRspDTO;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.KV.constant.ApiConstant;
import com.taoxin.communitysharing.KV.dto.response.FindSharingContentResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = ApiConstant.SERVICE_NAME)
public interface KVFeignApi {
    String PREFIX = "/kv";
    @PostMapping(value = PREFIX + "/sharing/content/add")
    Response<?> addSharingContent(@Validated @RequestBody AddSharingContentRequestDTO requestDTO);

    @PostMapping(value = PREFIX + "/sharing/content/find")
    Response<FindSharingContentResponseDTO> findSharingContent(@Validated @RequestBody FindSharingContentRequestDTO requestDTO);

    @PostMapping(value = PREFIX + "/sharing/content/delete")
    Response<?> deleteSharingContent(@Validated @RequestBody DeleteSharingContentRequestDTO requestDTO);

    @PostMapping(value = PREFIX + "/sharing/content/addBatchComment")
    Response<?> addBatchComment(@Validated @RequestBody AddBatchCommentContentReqDTO requestDTO);

    @PostMapping(value = PREFIX + "/sharing/content/batch/query")
    Response<List<FindCommentContentRspDTO>> batchQuery(@Validated @RequestBody BatchFindCommentContentReqDTO requestDTO);
}
