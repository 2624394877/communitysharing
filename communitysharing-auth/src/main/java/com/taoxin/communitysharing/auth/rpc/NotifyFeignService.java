package com.taoxin.communitysharing.auth.rpc;

import com.taoxin.communitysharing.notify.api.NotifyFeignApi;
import com.taoxin.communitysharing.notify.dto.NotifyDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotifyFeignService {
    @Resource
    private NotifyFeignApi notifyFeignApi;

    public void sendNotify(NotifyDTO notifyDTO) {
        notifyFeignApi.send(notifyDTO);
    }
}
