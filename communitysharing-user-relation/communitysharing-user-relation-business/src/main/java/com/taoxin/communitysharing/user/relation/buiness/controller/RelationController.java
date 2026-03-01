package com.taoxin.communitysharing.user.relation.buiness.controller;

import com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog;
import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.req.FindFansListReqVo;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.req.FollowingUserReqVo;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.req.FollowingUsersListReqVo;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.req.UnfollowUserReqVo;
import com.taoxin.communitysharing.user.relation.buiness.server.UserRelationServer;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relation")
public class RelationController {
    @Resource
    private UserRelationServer userRelationServer;

    @RequestMapping("/following")
    @ApiOperationLog(description = "关注用户")
    public Response<?> followUser(@Validated @RequestBody FollowingUserReqVo followingUserReqVo) {
        return userRelationServer.followUser(followingUserReqVo);
    }

    @RequestMapping("/unfollow")
    @ApiOperationLog(description = "取消关注")
    public Response<?> unfollowUser(@Validated @RequestBody UnfollowUserReqVo unfollowUserReqVo) {
        return userRelationServer.UnfollowUser(unfollowUserReqVo);
    }

    @RequestMapping("/followerList")
    @ApiOperationLog(description = "获取关注列表")
    public PageResponse<?> getFollowingList(@Validated @RequestBody FollowingUsersListReqVo followingUsersListReqVo) {
        return userRelationServer.getFollowingList(followingUsersListReqVo);
    }

    @RequestMapping("/fansList")
    @ApiOperationLog(description = "获取粉丝列表")
    public PageResponse<?> findFansList(@Validated @RequestBody FindFansListReqVo fansListReqVo) {
        return userRelationServer.findFansList(fansListReqVo);
    }
}
