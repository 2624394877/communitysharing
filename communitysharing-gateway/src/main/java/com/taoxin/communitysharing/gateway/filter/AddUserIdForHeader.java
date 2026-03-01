package com.taoxin.communitysharing.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AddUserIdForHeader implements GlobalFilter {
    private final static String USER_ID_HEADER = "userId";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) { // 过滤器在顺序执行过程中如果发生错误，会直接返回错误信息，不会继续执行下一个过滤器
        // 获取用户id
        String Authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.isEmpty(Authorization)) return chain.filter(exchange);
//        assert Authorization != null;
        String token = Authorization.substring(7);
        Object userid = null;
        Long userId = null;
        try {
//            userId = StpUtil.getLoginIdAsLong();
            userid = StpUtil.getLoginIdByToken(token);
            userId = Long.valueOf(userid.toString());
            log.info("=======> AddUserIdForHeader：{}",userId);
        } catch (Exception e) { // 获取用户id失败
            log.warn("获取用户ID失败: {}", e); // 添加日志
            return chain.filter(exchange);
        }
        Long finalUserId = userId;
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(USER_ID_HEADER, String.valueOf(finalUserId))) // 添加用户id
                .build();
        // 构建redis的token键
        return chain.filter(newExchange); // 继续执行下一个过滤器
    }
}
