package com.taoxin.communitysharing.user.business.service.Impl;

import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.taoxin.communitysharing.common.constant.DateConstants;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.common.uitl.DateUtil;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.common.uitl.NumberUtil;
import com.taoxin.communitysharing.count.model.vo.res.FindUserCountsByIdResVo;
import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import com.taoxin.communitysharing.user.business.constant.RedisKeyConstants;
import com.taoxin.communitysharing.user.business.domain.databaseObject.UserDo;
import com.taoxin.communitysharing.user.business.domain.mapper.UserDoMapper;
import com.taoxin.communitysharing.user.business.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.user.business.model.vo.req.FindUserInfoReqVo;
import com.taoxin.communitysharing.user.business.model.vo.res.FindUserInfoResVo;
import com.taoxin.communitysharing.user.business.rpc.CountFeignServcie;
import com.taoxin.communitysharing.user.business.service.UserInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor; // 线程池
    @Resource
    UserDoMapper userDoMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private CountFeignServcie countFeignServcie;

    // 初始化Caffeine本地缓存
    private static final Cache<Long, FindUserInfoResVo> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 初始缓存10000个元素
            .maximumSize(10000) // 最大缓存10000个元素
            .expireAfterWrite(5, TimeUnit.MINUTES) // 5分钟后失效
            .build();

    @Override
    public Response<FindUserInfoResVo> findUserInfo(FindUserInfoReqVo findUserInfoReqVo) {
        Long userId = findUserInfoReqVo.getUserId();

        if (Objects.isNull(userId)) {
            userId = LoginUserContextHolder.getUserId();
        }
        FindUserInfoResVo findUserInfoResVo = null;

        // 查本地缓存
        FindUserInfoResVo localCache = LOCAL_CACHE.getIfPresent(userId);
        if (Objects.nonNull(localCache)) {
            log.info("===【用户服务】===> 本地缓存命中：{}", localCache);
            return Response.success(localCache);
        }

        // 查redis
        String userHomeKey = RedisKeyConstants.getUserInfoMainKey(userId);
        String value = (String) redisTemplate.opsForValue().get(userHomeKey);
        if (StringUtils.isNotBlank(value)) {
            findUserInfoResVo = JsonUtil.parseObject(value, FindUserInfoResVo.class);
            asyncUserInfo2Local(userId, findUserInfoResVo);
            getSlefuserInfo(findUserInfoResVo,userId);
            return Response.success(findUserInfoResVo);
        }
        // 缓存中没有则查数据库
        UserDo user = userDoMapper.selectByPrimaryKey(userId);
        if (Objects.isNull(user)) throw new BusinessException(ResponseStatusEnum.USER_NOT_EXIST);
        findUserInfoResVo = FindUserInfoResVo.builder()
                .userId(user.getId())
                .communitysharingId(user.getCommunitysharingId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .backgroundImg(user.getBackgroundImg())
                .introduction(user.getIntroduction())
                .createTime(user.getCreateTime())
                .build();
        if (Objects.nonNull(user.getBirthday())) {
            Integer year = DateUtil.calculateYears(user.getBirthday());
            log.info("===【用户服务】===> 用户生日：{}", year);
            findUserInfoResVo.setYears(year);
        }else findUserInfoResVo.setYears(0);

        // 调用计数服务
        getRpcUserInfo(findUserInfoResVo,userId);

        // 保存到redis
        asyncUserInfo2Redis(findUserInfoResVo, userHomeKey);

        // 添加本地缓存
        asyncUserInfo2Local(userId, findUserInfoResVo);
        return Response.success(findUserInfoResVo);
    }

    private void getSlefuserInfo(FindUserInfoResVo findUserInfoResVo, Long userId) {
        if (Objects.equals(userId, LoginUserContextHolder.getUserId())) {
            getRpcUserInfo(findUserInfoResVo, userId);
        }
    }

    private void getRpcUserInfo(FindUserInfoResVo findUserInfoResVo, Long userId) {
        FindUserCountsByIdResVo findUserCountsByIdResVo = countFeignServcie.findUserCountsById(userId);
        if (Objects.nonNull(findUserCountsByIdResVo)) {
            findUserInfoResVo.setFansTotal(NumberUtil.formatNumberString(findUserCountsByIdResVo.getFansTotal()));
            findUserInfoResVo.setFollowingTotal(NumberUtil.formatNumberString(findUserCountsByIdResVo.getFollowingTotal()));
            findUserInfoResVo.setContentTotal(NumberUtil.formatNumberString(findUserCountsByIdResVo.getContentTotal()));
            findUserInfoResVo.setLikeTotal(NumberUtil.formatNumberString(findUserCountsByIdResVo.getContentTotal()));
            findUserInfoResVo.setCollectTotal(NumberUtil.formatNumberString(findUserCountsByIdResVo.getCollectTotal()));
        }
    }

    private void asyncUserInfo2Local(Long userId, FindUserInfoResVo findUserInfoResVo) {
        threadPoolTaskExecutor.execute(() -> {
            LOCAL_CACHE.put(userId, findUserInfoResVo);
        });
    }

    private void asyncUserInfo2Redis(FindUserInfoResVo findUserInfoResVo,String userHomeKey) {
        threadPoolTaskExecutor.execute(() -> {
            long expireTime = 60 * 60 * 12 + RandomUtil.randomInt(60 * 60);
            redisTemplate.opsForValue().set(userHomeKey, JsonUtil.toJsonString(findUserInfoResVo), expireTime, TimeUnit.SECONDS);
        });
    }
}
