package com.taoxin.communitysharing.content.business.rpc;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.count.api.CountFeignServer;
import com.taoxin.communitysharing.count.model.dto.Req.FindContentCountReqDTO;
import com.taoxin.communitysharing.count.model.dto.Res.FindContentCountResDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CountFeignApiServer {
    @Resource
    private CountFeignServer countFeignServer;

    public List<FindContentCountResDTO> findContentCount(List<Long> contentIds) {
        FindContentCountReqDTO reqDTO = new FindContentCountReqDTO();
        reqDTO.setContentId(contentIds);

        Response<List<FindContentCountResDTO>> response = countFeignServer.findContentCount(reqDTO);
        if (!response.isSuccess() || Objects.isNull(response.getData())) return null;

        return response.getData();
    }
}
