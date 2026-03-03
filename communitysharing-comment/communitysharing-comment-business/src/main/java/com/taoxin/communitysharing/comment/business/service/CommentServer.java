package com.taoxin.communitysharing.comment.business.service;

import com.taoxin.communitysharing.comment.business.model.vo.req.CommentPublishReqVo;
import com.taoxin.communitysharing.comment.business.model.vo.req.FindCommentPageListReqVo;
import com.taoxin.communitysharing.comment.business.model.vo.req.SecondCommentPageListReqVo;
import com.taoxin.communitysharing.comment.business.model.vo.res.FindCommentItemRspVo;
import com.taoxin.communitysharing.comment.business.model.vo.res.FindSecondCommentItemRspVo;
import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;

public interface CommentServer {
    /**
     * 发布评论
     * @param commentPublishReqVo 请求参数
     * @return 响应
     */
    Response<?> publishComtent(CommentPublishReqVo commentPublishReqVo);

    /**
     * 评论分页查询
     * @param findCommentPageListReqVo
     * @return
     */
    PageResponse<FindCommentItemRspVo> findCommentPageList(FindCommentPageListReqVo findCommentPageListReqVo);

    /**
     * 二级评论分页查询
     * @param secondCommentPageListReqVo
     * @return
     */
    PageResponse<FindSecondCommentItemRspVo> findChildCommentPageList(SecondCommentPageListReqVo secondCommentPageListReqVo);

}
