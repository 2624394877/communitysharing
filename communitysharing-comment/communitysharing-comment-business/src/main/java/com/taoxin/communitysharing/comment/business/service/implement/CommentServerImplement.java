package com.taoxin.communitysharing.comment.business.service.implement;

import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.taoxin.communitysharing.comment.business.constant.MQConstant;
import com.taoxin.communitysharing.comment.business.model.dto.PublishCommentMqDTO;
import com.taoxin.communitysharing.comment.business.model.vo.req.CommentPublishReqVo;
import com.taoxin.communitysharing.comment.business.model.vo.req.FindCommentPageListReqVo;
import com.taoxin.communitysharing.comment.business.model.vo.res.FindCommentItemRspVo;
import com.taoxin.communitysharing.comment.business.retry.SendMQRetryHelper;
import com.taoxin.communitysharing.comment.business.rpc.DistributedIdGeneratorRpcService;
import com.taoxin.communitysharing.comment.business.service.CommentServer;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class CommentServerImplement implements CommentServer {
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private SendMQRetryHelper sendMQRetryHelper;
    @Resource
    private DistributedIdGeneratorRpcService commentIdGenerator;
    @Override
    public Response<?> publishComtent(CommentPublishReqVo commentPublishReqVo) {
        String content = commentPublishReqVo.getContent();
        String imageUrl = commentPublishReqVo.getImageUrl();
        // 内容和图片不能都为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl), "评论不能为空");

        // todo 评论逻辑
        Long creatorId = LoginUserContextHolder.getUserId();
        String commentId = commentIdGenerator.getCommentId();
        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .contentId(commentPublishReqVo.getContentId())
                .commentId(Long.valueOf(commentId))
                .content(content)
                .imageUrl(imageUrl)
                .replayCommentId(commentPublishReqVo.getReplayCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();
        // 发送MQ
        sendMQRetryHelper.sendMQ(MQConstant.TOPIC_PUBLISH_COMMENT, JsonUtil.toJsonString(publishCommentMqDTO),"评论服务");
        return Response.success();
    }

    @Override
    public Response<FindCommentItemRspVo> findCommentPageList(FindCommentPageListReqVo findCommentPageListReqVo) {
        return null;
    }
}
