package com.taoxin.communitysharing.content.business.rpc;

import com.taoxin.communitysharing.distributed.id.constructor.api.IdConstructorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class IdConstructorFeignApiService {
    @Resource
    private IdConstructorFeignApi idConstructorFeignApi;

    public String getSegmentId() {
        return idConstructorFeignApi.getSegmentId("content");
    }

    public String getSnowflakeId() {
        return idConstructorFeignApi.getSnowflakeId("content");
    }
}
