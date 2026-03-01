package com.taoxin.communitysharing.search.api;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.search.constant.ApiConstant;
import com.taoxin.communitysharing.search.dto.request.RebuildContentDocReqDTO;
import com.taoxin.communitysharing.search.dto.request.RebuildUserDocReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstant.SERVICE_NAME)
public interface SearchFeignApi {
    String PREFIX = "/search";

    @PostMapping(value = PREFIX + "/content/doc/rebuild")
    Response<?> rebuildContentDoc(@RequestBody RebuildContentDocReqDTO reqDTO);

    @PostMapping(value = PREFIX + "/user/doc/rebuild")
    Response<?> rebuildUserDoc(@RequestBody RebuildUserDocReqDTO reqDTO);
}
