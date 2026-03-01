package com.taoxin.communitysharing.user.relation.buiness;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class TestGuava {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

//    @Test
//    public void testSend() {
//        for (long i = 0; i < 10000; i++) {
//            // 构建消息体 DTO
//            FollowUserMqDTO followUserMqDTO = FollowUserMqDTO.builder()
//                    .userId(i)
//                    .followingUserId(i)
//                    .createTime(LocalDateTime.now())
//                    .build();
//
//            // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
//            Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(followUserMqDTO))
//                    .build();
//
//            // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
//            String destination = MQConstant.FOLLOW_UNFOLLOW_TOPIC + ":" + MQConstant.FOLLOW_TAG;
//
//            log.info("==> 开始发送关注操作 MQ, 消息体: {}", followUserMqDTO);
//
//            // 异步发送 MQ 消息，提升接口响应速度
//            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
//                @Override
//                public void onSuccess(SendResult sendResult) {
//                    log.info("==> MQ 发送成功，SendResult: {}", sendResult);
//                }
//
//                @Override
//                public void onException(Throwable throwable) {
//                    log.error("==> MQ 发送异常: ", throwable);
//                }
//            });
//        }
//    }
}
