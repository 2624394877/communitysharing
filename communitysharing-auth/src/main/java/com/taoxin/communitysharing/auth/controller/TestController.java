package com.taoxin.communitysharing.auth.controller;


import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.taoxin.communitysharing.auth.alarm.AlarmInterface;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {
//    @NacosValue(value = "${rate-limit.api.limit}", autoRefreshed = true)
//    private Integer limit;

    @Resource
    AlarmInterface alarmInterface;

//    @GetMapping("/test")
//    public String test() {
//        return "接口限流阈值：" + limit;
//    }

    @GetMapping("/alarm")
    public String alarm() {
        alarmInterface.SendAlarm("测试告警");
        return "告警发送成功";
    }
}
