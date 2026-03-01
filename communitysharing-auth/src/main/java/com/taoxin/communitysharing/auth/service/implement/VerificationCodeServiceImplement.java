package com.taoxin.communitysharing.auth.service.implement;

import cn.hutool.core.util.RandomUtil;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.auth.Constant.RedisKeyConstants;
import com.taoxin.communitysharing.auth.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.auth.model.vo.verificationcode.SendVerificationCodeReqVo;
import com.taoxin.communitysharing.auth.model.vo.verificationcode.SendVerificationCodeReqVoSMTP;
import com.taoxin.communitysharing.auth.service.VerificationCodeService;
import com.taoxin.communitysharing.auth.sms.AliyunSmsHelper;
import com.taoxin.communitysharing.auth.smtp.EmailSmtpHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class VerificationCodeServiceImplement implements VerificationCodeService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate; // 引入RedisTemplate
    @Resource
    private AliyunSmsHelper aliyunSmsHelper; // 引入阿里云短信服务
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor; // 引入线程池 进行异步线程执行
    @Resource
    private EmailSmtpHelper emailSmtpHelper;

    private VerificationCodeServiceImplement() {
    }


    /**
     * 发送手机验证码
     * @param sendVerificationCodeReqVo 发送验证码请求参数
     * @return Response<?> 响应对象
     */
    @Override
    public Response<?> sendVerificationCode(SendVerificationCodeReqVo sendVerificationCodeReqVo) {
        // 手机号
        String phone = sendVerificationCodeReqVo.getPhone();
        // 构建验证码
        String verificationCode = RedisKeyConstants.getVerificationCodeKey(phone);
        // 判断是否需要发送验证码
        boolean isNeedSend = redisTemplate.hasKey(verificationCode);
        if (isNeedSend) {
            throw new BusinessException(ResponseStatusEnum.TOO_MANY_REQUESTS); // 抛出业务异常
        }
        // 生成验证码 （6为随机数字）
        String code = RandomUtil.randomNumbers(6);
        // 调用第三方服务验证码服务
        threadPoolTaskExecutor.submit(()-> {
            String signName = "速通互联验证码";
            String templateCode = "100001";
            String templateParam = String.format("{\"code\":\"%s\",\"min\":\"3\"}",code); // 模板参数
            aliyunSmsHelper.sendSms(signName,templateCode,phone,templateParam);
        });
        // 日志打印
        log.info("{'手机号':{},'验证码': {},已发送}",phone,code);
        // 存储验证码
        redisTemplate.opsForValue().set(verificationCode,code,3, TimeUnit.MINUTES);
        return Response.success();
    }

    @Override
    public Response<?> sendVerificationCode(SendVerificationCodeReqVoSMTP sendVerificationCodeReqVoSMTP) {
        // 邮箱
        String email = sendVerificationCodeReqVoSMTP.getEmail();
        // 构建验证码
        String verificationCode = RedisKeyConstants.getVerificationCodeKey(email);
        // 判断是否需要发送验证码
        boolean isNeedSend = redisTemplate.hasKey(verificationCode);
        if (isNeedSend) {
            throw new BusinessException(ResponseStatusEnum.TOO_MANY_REQUESTS);
        }
        // 生成验证码 （6为随机数字）
        String code = RandomUtil.randomNumbers(6);
        // 调用第三方服务验证码服务
        // 为什么要用线程池？ 因为发送邮件是一个耗时操作，如果直接在主线程中执行，会导致主线程阻塞，从而导致请求超时
        threadPoolTaskExecutor.submit(()->{
            emailSmtpHelper.sendEmail(email,"平台验证码",String.format("您的验证码为：%s",code));
        });
        // 打印日志
        log.info("{'邮箱':{},'验证码': {},已发送}",email,code);
        // 存储验证码
        redisTemplate.opsForValue().set(verificationCode,code,3, TimeUnit.MINUTES);
        return Response.success();
    }
}


