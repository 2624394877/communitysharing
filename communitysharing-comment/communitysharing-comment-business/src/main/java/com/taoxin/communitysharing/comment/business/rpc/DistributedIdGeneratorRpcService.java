package com.taoxin.communitysharing.comment.business.rpc;

import com.taoxin.communitysharing.distributed.id.constructor.api.IdConstructorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class DistributedIdGeneratorRpcService {
    @Resource
    private IdConstructorFeignApi idConstructorFeignApi;

    /**
     * 生成评论id
     * @return 评论id
     */
    public String getCommentId() {
        return idConstructorFeignApi.getSegmentId("leaf-segment-comment-id");
    }
}
