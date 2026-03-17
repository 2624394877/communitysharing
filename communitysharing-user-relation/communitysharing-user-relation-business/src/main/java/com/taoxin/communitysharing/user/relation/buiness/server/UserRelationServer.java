package com.taoxin.communitysharing.user.relation.buiness.server;

import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.req.*;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.res.FindFansUsersListRseVo;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.res.FollowStatusJudgeResVo;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.res.FollowingUsersListResVo;

public interface UserRelationServer {

    /**
     * 关注用户
     * @param followingUserReqVo 关注用户请求参数
     * @return 响应对象
     */
    Response<?> followUser(FollowingUserReqVo followingUserReqVo);

    /**
     * 取消关注用户
     * @param unfollowUserReqVo 取消关注用户请求参数
     * @return 响应对象
     */
    Response<?> UnfollowUser(UnfollowUserReqVo unfollowUserReqVo);

    /**
     * 获取用户关注列表
     * @param followingUsersListReqVo 用户参数
     * @return 响应对象
     */
    PageResponse<FollowingUsersListResVo> getFollowingList(FollowingUsersListReqVo followingUsersListReqVo);

    /**
     * 获取用户粉丝列表
     * @param findFansListReqVO 用户参数
     * @return 响应对象
     */
    PageResponse<FindFansUsersListRseVo> findFansList(FindFansListReqVo findFansListReqVO);

    Response<FollowStatusJudgeResVo> judgeFollowStatus(FollowStatusJudgeReqVo followStatusJudgeReqVo);
}
