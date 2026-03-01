package com.taoxin.communitysharing.notify.business.service;

import com.taoxin.communitysharing.notify.business.handler.NotifyWebSocketHandler;
import com.taoxin.communitysharing.notify.business.model.dto.NotifyDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class WebSocketPushLoginService {
    @Resource
    private NotifyWebSocketHandler notifyWebSocketHandler;

    public void sendKickoutMessage(Long userId) {
        try {
            NotifyDTO notifyDTO = NotifyDTO.builder()
                    .userId(userId)
                    .content("您的账号在其他设备登录，您已被迫下线！")
                    .build();
            notifyWebSocketHandler.sendToUser(notifyDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
