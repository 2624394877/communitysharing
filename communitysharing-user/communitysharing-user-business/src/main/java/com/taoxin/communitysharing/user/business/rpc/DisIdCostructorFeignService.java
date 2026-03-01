package com.taoxin.communitysharing.user.business.rpc;

import com.taoxin.communitysharing.distributed.id.constructor.api.IdConstructorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class DisIdCostructorFeignService {
    @Resource
    private IdConstructorFeignApi idConstructorFeignApi;

    private static final String BIZ_TAG_COMMUNITYSHARING_ID = "leaf-segment-communitysharing-id";

    private static final String BIZ_TAG_COMMUNITYSHARING_USER_ID = "leaf-segment-communitysharing-user-id";

    public String getSegmentId() {
        return idConstructorFeignApi.getSegmentId(BIZ_TAG_COMMUNITYSHARING_ID);
    }

    public String getUserId() {
        return idConstructorFeignApi.getSnowflakeId(BIZ_TAG_COMMUNITYSHARING_USER_ID);
    }

    public String getSnowflakeId() {
        return idConstructorFeignApi.getSnowflakeId(BIZ_TAG_COMMUNITYSHARING_ID);
    }
}
