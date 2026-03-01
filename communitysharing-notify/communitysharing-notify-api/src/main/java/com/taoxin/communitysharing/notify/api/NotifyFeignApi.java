package com.taoxin.communitysharing.notify.api;

import com.taoxin.communitysharing.notify.constant.ApiConstant;
import com.taoxin.communitysharing.notify.dto.NotifyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstant.SERVICE_NAME)
public interface NotifyFeignApi {
    String PREFIX = "/notify";

    @PostMapping(value = PREFIX + "/send")
    void send(@RequestBody NotifyDTO notifyDTO);
}
