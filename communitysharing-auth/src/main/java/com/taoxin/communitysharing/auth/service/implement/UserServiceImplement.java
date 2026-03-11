package com.taoxin.communitysharing.auth.service.implement;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.common.uitl.ParamsUtil;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByPhoneResDTO;
import com.google.common.base.Preconditions;
import com.taoxin.communitysharing.auth.Constant.RedisKeyConstants;
import com.taoxin.communitysharing.auth.enums.LoginTypeEnum;
import com.taoxin.communitysharing.auth.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.auth.model.vo.user.UserLoginRequestVO;
import com.taoxin.communitysharing.auth.model.vo.user.UserPasswordReqVo;
import com.taoxin.communitysharing.auth.model.vo.user.UserloginRequestByEmailVo;
import com.taoxin.communitysharing.auth.rpc.NotifyFeignService;
import com.taoxin.communitysharing.auth.rpc.UserFeignService;
import com.taoxin.communitysharing.auth.service.UserService;
import com.taoxin.communitysharing.notify.dto.NotifyDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImplement implements UserService {
    @Resource
    private RedisTemplate<String,Object> redisTemplate; // 将token存储在redis中
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserFeignService userFeignService;
    @Resource
    private NotifyFeignService notifyFeignService;

    @Override
    public Response<?> PasswordUpdate(UserPasswordReqVo userPasswordReqVo) {
        log.info("=======> 修改密码: {}", userPasswordReqVo.getNewPassword());
        /** 使用使用 BCryptPasswordEncoder 对密码进行加密 **/
        // 拿到参数
        String newPassword = userPasswordReqVo.getNewPassword();
        // 对密码做正则校验
        Preconditions.checkArgument(ParamsUtil.isValidPassword(newPassword), ResponseStatusEnum.PASSWORD_FORMAT_ERROR.getErrorMessage());
        // 进行加密
        String encode = passwordEncoder.encode(newPassword);
        // 保存密码
        return userFeignService.updateUserPassword(encode);
    }

    @Override
    public Response<?> outLogin() {
        StpUtil.logout(LoginUserContextHolder.getUserId());
        return Response.success();
    }

    @Override
    public Response<?> LoginAndRegister(UserLoginRequestVO userLoginRequestVO) {
        Integer type = userLoginRequestVO.getType(); // 登录方式
        LoginTypeEnum loginTypeEnum = LoginTypeEnum.getByCode(type); // 登录方式枚举
        Long userId = null; // 用户id
        String code = null; // 验证码
        String key = null; // redis中的验证码的key
        String redisCode = null; // redis中的验证码
        String phone = userLoginRequestVO.getPhone(); // 手机号
        switch (loginTypeEnum) {
            case PHONE_NUMBER: // 手机号登录
                code = userLoginRequestVO.getCode();
                Preconditions.checkArgument(StringUtils.isNotBlank(code), "验证码不能为空");
                // 构建redis中的验证码的key
                key = RedisKeyConstants.getVerificationCodeKey(phone);
                // 获取redis中的验证码
                redisCode = (String) redisTemplate.opsForValue().get(key);
                if (!StringUtils.equals(code, redisCode)) {
                    // 验证码不一致则抛出验证码错误异常
                    throw new BusinessException(ResponseStatusEnum.VERIFICATION_CODE_ERROR);
                }
                // 调用用户服务注册用户
                Long userIdFeign = userFeignService.register(phone);
                if (Objects.isNull(userIdFeign)) {
                    throw new BusinessException(ResponseStatusEnum.LOGIN_FAILED);
                }
                userId = userIdFeign;
                break;
            case PASSWORD: // 密码登录
                /* 思路： 在登录前先判断用户是否存在，由于密码是crypto加密的，所以需要先拿出密文，再进行解密进行匹配 */
                // 1. 根据手机号查询用户
                FindUserByPhoneResDTO findUserByPhoneResDTO = userFeignService.findUserByPhone(phone);
                log.info("用户手机号{}查询结果：{}", phone, JsonUtil.toJsonString(findUserByPhoneResDTO)); // 打印查询结果
                if (Objects.isNull(findUserByPhoneResDTO)) {
                    throw new BusinessException(ResponseStatusEnum.USER_NOT_FOUND); // 用户不存在则抛出用户不存在异常
                }
                // 2. 获取密文，匹配密码
                String encodePassword = findUserByPhoneResDTO.getPassword();
                if (!passwordEncoder.matches(userLoginRequestVO.getPassword(), encodePassword)) {
                    throw new BusinessException(ResponseStatusEnum.PASSWORD_ERROR);
                }
                // 3. 拿得用户id
                userId = findUserByPhoneResDTO.getId();
                break;
            default: // 这里直接break, 因为只定义了三个方式
                break;
        }

        // 用sa-token自动登录
        StpUtil.login(userId, new SaLoginModel().setIsLastingCookie(true));

        NotifyDTO notifyDTO = NotifyDTO.builder()
                .userId(userId)
                .content("您的账号于"+ JsonUtil.toJsonString(LocalDateTime.now()) +"登录成功，如非本人操作，请及时修改手机号保障账号安全！")
                .title("Login")
                .build();

        notifyFeignService.sendNotify(notifyDTO);

        // 获取token令牌
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return Response.success(tokenInfo.tokenValue);
    }

    @Override
    public Response<?> LoginByEmail(UserloginRequestByEmailVo userloginRequestByEmailVo) {
        String email = userloginRequestByEmailVo.getEmail(); // 邮箱
        String code = userloginRequestByEmailVo.getCode(); // 验证码
        Response<Long> response = userFeignService.findUserByEmail(email);
        log.info("用户邮箱{}查询结果：{}", response);
        Long userIdFeign = null;
        if (Objects.nonNull(response) && response.isSuccess()) {
            userIdFeign = response.getData();
        } else throw new BusinessException(ResponseStatusEnum.USER_NOT_FOUND.getErrorCode(),response.getMessage());
        Long userId = null; // 用户id
        if (StringUtils.isBlank(code)) { // 判断验证码是否为空，空则抛出参数无效异常
            throw new BusinessException(ResponseStatusEnum.PARAMS_NOT_VALID);
        }
        String key = RedisKeyConstants.getVerificationCodeKey(email);
        String redisCode = (String) redisTemplate.opsForValue().get(key);
        if (!StringUtils.equals(code, redisCode)) { // 验证码不一致则抛出验证码错误异常
            throw new BusinessException(ResponseStatusEnum.VERIFICATION_CODE_ERROR);
        }
        userId = userIdFeign;
        // 用sa-token自动登录
        StpUtil.login(userId, new SaLoginModel().setIsLastingCookie(true));

        NotifyDTO notifyDTO = NotifyDTO.builder()
                .userId(userId)
                .content("您的账号于"+ JsonUtil.toJsonString(LocalDateTime.now()) +"登录成功，如非本人操作，请及时修改手机号保障账号安全！")
                .title("Login")
                .build();

        notifyFeignService.sendNotify(notifyDTO);

        // 获取token令牌
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return Response.success(tokenInfo.tokenValue);
    }
}
