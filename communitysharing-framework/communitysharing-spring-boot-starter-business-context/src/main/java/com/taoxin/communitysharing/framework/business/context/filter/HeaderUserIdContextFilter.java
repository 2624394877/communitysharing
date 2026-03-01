package com.taoxin.communitysharing.framework.business.context.filter;

import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import org.apache.commons.lang3.StringUtils;
import com.taoxin.communitysharing.common.constant.GlobalConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @OncePerRequestFilter 保证过滤器只被调用一次
 */
@Slf4j
public class HeaderUserIdContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader(GlobalConstants.USER_ID); // 获取用户id
        if (StringUtils.isBlank(userId)) { // 用户id不为空
            filterChain.doFilter(request, response);
            return;
        }
        log.info("=======> HeaderUserIdContextFilter,userId: {}",userId);
        LoginUserContextHolder.setUserId(userId);
        // 如果 header 中存在 userId，则设置到 ThreadLocal 中
        try {
            filterChain.doFilter(request, response); // 继续执行下一个过滤器
        } finally {
            // 一定要删除 ThreadLocal ，防止内存泄露
            LoginUserContextHolder.remove();
            log.info("=======> 删除 ThreadLocal， userId: {}", userId);
        }
    }
}
