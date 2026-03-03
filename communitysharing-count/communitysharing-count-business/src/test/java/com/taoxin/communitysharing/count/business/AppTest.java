package com.taoxin.communitysharing.count.business;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.count.business.model.dto.LikeUnLikeCommentMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;

@SpringBootTest
@Slf4j
public class AppTest {
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    /**
     * 测试：模拟发送评论点赞、取消点赞消息
     */
    @Test
    void testBatchSendLikeUnlikeCommentMQ() {
        Long userId = 27L;
        Long commentId = 30002L;

        for (long i = 0; i < 32; i++) {
            // 构建消息体 DTO
            LikeUnLikeCommentMqDTO likeUnlikeCommentMqDTO = LikeUnLikeCommentMqDTO.builder()
                    .userId(userId)
                    .commentId(commentId)
                    .createTime(LocalDateTime.now())
                    .build();

            // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
            String destination = "commentLikeTopic:";

            if (i % 2 == 0) { // 偶数
                likeUnlikeCommentMqDTO.setType(0); // 取消点赞
                destination = destination + "unlike";
            } else { // 奇数
                likeUnlikeCommentMqDTO.setType(1); // 点赞
                destination = destination + "like";
            }

            // MQ 分区键
            String hashKey = String.valueOf(userId);

            // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
            Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(likeUnlikeCommentMqDTO))
                    .build();

            // 同步发送 MQ 消息
            rocketMQTemplate.syncSendOrderly(destination, message, hashKey);
        }
    }

}
