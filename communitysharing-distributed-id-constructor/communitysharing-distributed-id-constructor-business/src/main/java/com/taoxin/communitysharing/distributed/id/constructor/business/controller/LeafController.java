package com.taoxin.communitysharing.distributed.id.constructor.business.controller;

import com.taoxin.communitysharing.distributed.id.constructor.business.core.common.Result;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.common.Status;
import com.taoxin.communitysharing.distributed.id.constructor.business.exception.LeafServerException;
import com.taoxin.communitysharing.distributed.id.constructor.business.exception.NoKeyException;
import com.taoxin.communitysharing.distributed.id.constructor.business.service.SegmentService;
import com.taoxin.communitysharing.distributed.id.constructor.business.service.SnowflakeService;
import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/id")
public class LeafController {
    private Logger logger = LoggerFactory.getLogger(LeafController.class);

    @Autowired
    private SegmentService segmentService;
    @Autowired
    private SnowflakeService snowflakeService;

    @RequestMapping(value = "/segment/get/{key}")
    @ApiOperationLog(description = "获取segment id")
    public String getSegmentId(@PathVariable("key") String key) {
        return get(key, segmentService.getId(key));
    }

    @RequestMapping(value = "/snowflake/get/{key}")
    @ApiOperationLog(description = "获取snowflake id")
    public String getSnowflakeId(@PathVariable("key") String key) {
        return get(key, snowflakeService.getId(key));
    }

    private String get(@PathVariable("key") String key, Result id) {
        Result result;
        if (key == null || key.isEmpty()) {
            throw new NoKeyException();
        }
        result = id;
        if (result.getStatus().equals(Status.EXCEPTION)) {
            throw new LeafServerException(result.toString());
        }
        return String.valueOf(result.getId());
    }
}
