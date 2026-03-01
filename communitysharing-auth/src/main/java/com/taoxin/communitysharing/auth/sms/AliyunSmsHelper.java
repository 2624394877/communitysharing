package com.taoxin.communitysharing.auth.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component // 标记为组件
@Slf4j
public class AliyunSmsHelper {
    @Resource
    private Client client;

    public boolean sendSms(String signName, String templateCode, String phone, String templateParam) {
        // 创建发送短信的请求
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setPhoneNumber(phone)
                .setTemplateParam(templateParam);
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            log.info("开始发送短信验证, 手机号: {}, 签名: {}, 模板代码: {}, 模板参数: {}", phone, signName, templateCode, templateParam);
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(request, runtime);
            log.info("发送短信验证成功, 响应: {}", JsonUtil.toJsonString(response));
            return true;
        } catch (Exception e) {
            log.error("发送短信验证失败: {}", e.getMessage());
            return false;
        }
    }
}
