package com.taoxin.communitysharing.search.business.controller;

import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import com.taoxin.communitysharing.search.business.model.vo.req.SearchContentReqVo;
import com.taoxin.communitysharing.search.business.model.vo.req.SearchUserReqVo;
import com.taoxin.communitysharing.search.business.model.vo.res.SearchContentResVo;
import com.taoxin.communitysharing.search.business.model.vo.res.SearchUserResVo;
import com.taoxin.communitysharing.search.business.service.ContentServer;
import com.taoxin.communitysharing.search.business.service.ExtDictService;
import com.taoxin.communitysharing.search.business.service.UserServer;
import com.taoxin.communitysharing.search.dto.request.RebuildContentDocReqDTO;
import com.taoxin.communitysharing.search.dto.request.RebuildUserDocReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {
    @Resource
    private UserServer userServer;
    @Resource
    private ContentServer contentServer;
    @Resource
    private ExtDictService extDictService;

    @PostMapping("/user")
    @ApiOperationLog(description = "用户搜索")
    public PageResponse<SearchUserResVo> searchUser(@Validated @RequestBody SearchUserReqVo reqVo) {
        return userServer.searchUser(reqVo);
    }

    @PostMapping("/content")
    @ApiOperationLog(description = "内容搜索")
    public PageResponse<SearchContentResVo> searchContent(@Validated @RequestBody SearchContentReqVo reqVo) {
        return contentServer.searchContent(reqVo);
    }

    @GetMapping("/hot")
    @ApiOperationLog(description = "获取热门搜索")
    public ResponseEntity<String> getHotSearch() {
        return extDictService.getHotUpdateExtDict();
    }

    /* ---------------------------------下面是其他服务调用接口------------------------------------ */
    @PostMapping("/content/doc/rebuild")
    @ApiOperationLog(description = "重建内容文档")
    public Response<Long> rebuildDocument(@Validated @RequestBody RebuildContentDocReqDTO reqDTO) {
        return contentServer.rebuildDocument(reqDTO);
    }

    @PostMapping("/user/doc/rebuild")
    @ApiOperationLog(description = "重建用户文档")
    public Response<Long> rebuildDocument(@Validated @RequestBody RebuildUserDocReqDTO reqDTO) {
        return userServer.rebuildDocument(reqDTO);
    }
}
