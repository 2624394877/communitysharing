package com.taoxin.communitysharing.count.business.controller;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.count.business.model.vo.req.FindUserCountsByIdReqVo;
import com.taoxin.communitysharing.count.business.model.vo.res.FindUserCountsByIdResVo;
import com.taoxin.communitysharing.count.business.service.CountServer;
import com.taoxin.communitysharing.count.model.dto.Req.FindContentCountReqDTO;
import com.taoxin.communitysharing.count.model.dto.Res.FindContentCountResDTO;
import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CountController {
    @Resource
    private CountServer countServer;

    @PostMapping("/user/data")
    @ApiOperationLog(description = "查询用户计数数据")
    public Response<FindUserCountsByIdResVo> findUserCountsById(@Validated @RequestBody FindUserCountsByIdReqVo reqVo) {
        return countServer.findUserCountsById(reqVo);
    }

    @PostMapping("/content/data")
    @ApiOperationLog(description = "查询内容计数数据")
    public Response<List<FindContentCountResDTO>> findContentCount(@Validated @RequestBody FindContentCountReqDTO reqVo) {
        return countServer.findContentCount(reqVo);
    }
}
