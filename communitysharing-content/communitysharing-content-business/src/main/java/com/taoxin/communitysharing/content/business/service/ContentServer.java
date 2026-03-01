package com.taoxin.communitysharing.content.business.service;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.content.business.model.vo.ContentPublishVo;
import com.taoxin.communitysharing.content.business.model.vo.req.*;
import com.taoxin.communitysharing.content.business.model.vo.res.ContentDetailsResVo;

public interface ContentServer {

    Response<?> PublishContent(ContentPublishVo contentPublishVo);

    Response<ContentDetailsResVo> ContentDetails(ContentDetailsReqVo contentDetailsReqVo);

    Response<?> UpdateContent(ContentUpdateReqVo contentUpdateReqVo);

    Response<?> DeleteContent(ContentDeleteReqVo contentDeleteReqVo);

    void DeleteContentLocalCache(Long contentId);

    Response<?> SetContentPrivateOfVisible(ContentPrivateOfVisibleReqVo contentPrivateOfVisibleReqVo);

    Response<?> SetContentIsTop(ContentIsTopReqVo contentIsTopReqVo);

    Response<?> ContentLike(ContentLikeReqVo contentLikeReqVo);

    Response<?> ContentUnlike(ContentUnlikeReqVo contentUnlikeReqVo);

    Response<?> CollectContent(CollectContentReqVo collectContentReqVo);

    Response<?> UnCollectContent(UnCollectContentReqVo unCollectContentReqVo);
}
