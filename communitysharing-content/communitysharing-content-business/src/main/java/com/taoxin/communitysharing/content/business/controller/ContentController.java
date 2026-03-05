package com.taoxin.communitysharing.content.business.controller;

import com.taoxin.communitysharing.content.business.model.vo.res.ContentPublishListResVo;
import com.taoxin.communitysharing.content.business.model.vo.res.LikeCollectStatusJudgeResVo;
import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.content.business.model.vo.ContentPublishVo;
import com.taoxin.communitysharing.content.business.model.vo.req.*;
import com.taoxin.communitysharing.content.business.model.vo.res.ContentDetailsResVo;
import com.taoxin.communitysharing.content.business.service.ContentServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/content")
@Slf4j
public class ContentController {
    @Resource
    private ContentServer contentServer;
    @RequestMapping("/publish")
    @ApiOperationLog(description = "发布内容")
    public Response<?> publish(@Validated @RequestBody ContentPublishVo contentPublishVo) {
        return contentServer.PublishContent(contentPublishVo);
    }

    @RequestMapping("/details")
    @ApiOperationLog(description = "内容详情")
    public Response<ContentDetailsResVo> details(@Validated @RequestBody ContentDetailsReqVo contentDetailsReqVo) {
        return contentServer.ContentDetails(contentDetailsReqVo);
    }

    @RequestMapping("/update")
    @ApiOperationLog(description = "更新内容")
    public Response<?> update(@Validated @RequestBody ContentUpdateReqVo contentUpdateReqVo) {
        return contentServer.UpdateContent(contentUpdateReqVo);
    }

    @RequestMapping("/delete")
    @ApiOperationLog(description = "删除内容")
    public Response<?> delete(@Validated @RequestBody ContentDeleteReqVo contentDeleteReqVo) {
        return contentServer.DeleteContent(contentDeleteReqVo);
    }

    @RequestMapping("/visible/private")
    @ApiOperationLog(description = "设置内容私有可见")
    public Response<?> setContentPrivateOfVisible(@Validated @RequestBody ContentPrivateOfVisibleReqVo contentPrivateOfVisibleReqVo) {
        return contentServer.SetContentPrivateOfVisible(contentPrivateOfVisibleReqVo);
    }

    @RequestMapping("/top")
    @ApiOperationLog(description = "设置内容置顶")
    public Response<?> setContentIsTop(@Validated @RequestBody ContentIsTopReqVo contentIsTopReqVo) {
        return contentServer.SetContentIsTop(contentIsTopReqVo);
    }

    @RequestMapping("/like")
    @ApiOperationLog(description = "内容点赞")
    public Response<?> like(@Validated @RequestBody ContentLikeReqVo contentLikeReqVo) {
        return contentServer.ContentLike(contentLikeReqVo);
    }

    @RequestMapping("/unlike")
    @ApiOperationLog(description = "内容取消点赞")
    public Response<?> unlike(@Validated @RequestBody ContentUnlikeReqVo contentUnlikeReqVo) {
        return contentServer.ContentUnlike(contentUnlikeReqVo);
    }

    @RequestMapping("/collect")
    @ApiOperationLog(description = "内容收藏")
    public Response<?> collect(@Validated @RequestBody CollectContentReqVo collectContentReqVo) {
        return contentServer.CollectContent(collectContentReqVo);
    }

    @RequestMapping("/uncollect")
    @ApiOperationLog(description = "内容取消收藏")
    public Response<?> unCollect(@Validated @RequestBody UnCollectContentReqVo unCollectContentReqVo) {
        return contentServer.UnCollectContent(unCollectContentReqVo);
    }

    @RequestMapping("/judge/likeAndCollect")
    @ApiOperationLog(description = "判断内容是否点赞和收藏")
    public Response<LikeCollectStatusJudgeResVo> judgeLikeAndCollect(@Validated @RequestBody LikeCollectStatusJudge likeCollectStatusJudgeReqVo) {
        return contentServer.LikeCollectStatusJudge(likeCollectStatusJudgeReqVo);
    }

    @RequestMapping("/publish/list")
    @ApiOperationLog(description = "内容发布列表")
    public Response<ContentPublishListResVo> publishList(@Validated @RequestBody ContentPublishListReqVo contentPublishListReqVo) {
        return contentServer.ContentPublishList(contentPublishListReqVo);
    }
}
