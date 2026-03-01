package com.taoxin.communitysharing.notify.business.controller;

import com.taoxin.communitysharing.notify.business.handler.NotifyWebSocketHandler;
import com.taoxin.communitysharing.notify.business.model.dto.NotifyDTO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Resource
    private NotifyWebSocketHandler webSocketHandler;

    @PostMapping("/send")
    public void send(@RequestBody NotifyDTO notifyDTO) throws IOException {
        webSocketHandler.sendToUser(notifyDTO);
    }
}
