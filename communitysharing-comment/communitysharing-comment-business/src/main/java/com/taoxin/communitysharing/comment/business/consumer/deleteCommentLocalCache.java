package com.taoxin.communitysharing.comment.business.consumer;

import com.taoxin.communitysharing.comment.business.constant.MQConstant;
import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentDo;
import com.taoxin.communitysharing.comment.business.model.vo.req.DeleteCommentReqVo;
import com.taoxin.communitysharing.comment.business.service.CommentServer;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RocketMQMessageListener(
        consumerGroup = "communitysharing_group_" + MQConstant.TOPIC_COMMENT_DELETE,
        topic = MQConstant.TOPIC_COMMENT_DELETE,
        messageModel = MessageModel.BROADCASTING // 广播模式
)
public class deleteCommentLocalCache implements RocketMQListener<String> {
    @Resource
    private CommentServer commentServer;

    @Override
    public void onMessage(String s) {
        log.info("【删除评论】 删除评论缓存, commentId: {}", s);
        DeleteCommentReqVo commentDo = JsonUtil.parseObject(s, DeleteCommentReqVo.class);
        if (Objects.nonNull(commentDo)) {
            commentServer.deleteCommentLocalCache(commentDo.getCommentId());
        }
    }
}
