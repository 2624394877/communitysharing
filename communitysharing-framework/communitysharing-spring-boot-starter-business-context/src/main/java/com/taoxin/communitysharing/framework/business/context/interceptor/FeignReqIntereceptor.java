package com.taoxin.communitysharing.framework.business.context.interceptor;

import com.taoxin.communitysharing.common.constant.GlobalConstants;
import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class FeignReqIntereceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Long userId = LoginUserContextHolder.getUserId();
        if (Objects.nonNull(userId)) {
            requestTemplate.header(GlobalConstants.USER_ID, userId.toString());
        }
    }
}
