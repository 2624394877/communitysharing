package com.taoxin.communitysharing.auth.alarm.impl;

import com.taoxin.communitysharing.auth.alarm.AlarmInterface;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MailAlarmHelper  implements AlarmInterface {
    @Override
    public boolean SendAlarm(String message){
        log.info("[邮件告警信息]: {}", message);
        // todo

        return true;
    }
}
