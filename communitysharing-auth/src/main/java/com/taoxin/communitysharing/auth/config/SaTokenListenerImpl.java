package com.taoxin.communitysharing.auth.config;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.SaLoginModel;
import com.taoxin.communitysharing.auth.rpc.NotifyFeignService;
import com.taoxin.communitysharing.notify.dto.NotifyDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SaTokenListenerImpl implements SaTokenListener {
    @Resource
    private NotifyFeignService notifyFeignService;

    @Override
    public void doLogin(String s, Object o, String s1, SaLoginModel saLoginModel) {

    }

    @Override
    public void doLogout(String s, Object o, String s1) {

    }

    @Override
    public void doKickout(String s, Object o, String s1) {

    }

    @Override
    public void doReplaced(String s, Object o, String s1) {
        log.info("===》 doLogout:{}",o);
        notifyFeignService.sendNotify(NotifyDTO.builder()
                .userId(Long.valueOf(o.toString()))
                .title("kickOut")
                .content("您的账号在其他设备登录，您已被迫下线！")
                .build());
    }

    @Override
    public void doDisable(String s, Object o, String s1, int i, long l) {

    }

    @Override
    public void doUntieDisable(String s, Object o, String s1) {

    }

    @Override
    public void doOpenSafe(String s, String s1, String s2, long l) {

    }

    @Override
    public void doCloseSafe(String s, String s1, String s2) {

    }

    @Override
    public void doCreateSession(String s) {

    }

    @Override
    public void doLogoutSession(String s) {

    }

    @Override
    public void doRenewTimeout(String s, Object o, long l) {

    }
}
