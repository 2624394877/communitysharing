package com.taoxin.communitysharing.count.api;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.count.constant.ApiConstant;
import com.taoxin.communitysharing.count.model.dto.Req.FindContentCountReqDTO;
import com.taoxin.communitysharing.count.model.dto.Res.FindContentCountResDTO;
import com.taoxin.communitysharing.count.model.vo.req.FindUserCountsByIdReqVo;
import com.taoxin.communitysharing.count.model.vo.res.FindUserCountsByIdResVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = ApiConstant.SERVICE_NAME)
public interface CountFeignServer {

//    String PREFIX = "/count";

    @PostMapping( "/user/data")
    Response<FindUserCountsByIdResVo> findUserCountsById(@Validated @RequestBody FindUserCountsByIdReqVo requestDTO);

    @PostMapping("/content/data")
    Response<List<FindContentCountResDTO>> findContentCount(@Validated @RequestBody FindContentCountReqDTO reqDTO);
}
