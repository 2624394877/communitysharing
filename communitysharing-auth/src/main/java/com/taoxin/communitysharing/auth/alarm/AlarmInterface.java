package com.taoxin.communitysharing.auth.alarm;

public interface AlarmInterface {

    /**
     * 发送告警
     * @param message 告警信息
     * @return 是否发送成功
     */
    boolean SendAlarm(String message);
}
