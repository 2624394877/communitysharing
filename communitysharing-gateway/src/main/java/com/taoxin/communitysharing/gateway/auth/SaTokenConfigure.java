package com.taoxin.communitysharing.gateway.auth;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [Sa-Token 权限认证] 配置类
 * @author click33
 */
@Slf4j
@Configuration
public class SaTokenConfigure {
    // 注册 Sa-Token全局过滤器
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")    /* 拦截全部path */
                // 开放地址
                .addExclude("/favicon.ico")
                // 鉴权方法：每次访问进入
                .setAuth(obj -> {
//                    log.info("[Sa-Token] 每次请求进入: {}", SaHolder.getRequest().getRequestPath());
                    SaRouter.match("/**")
                            .notMatch("/ws")
                            .notMatch("/ws/**")
                            .notMatch("/notify/**")
                            .notMatch("/auth/login")
                            .notMatch("/auth/loginByEmail")
                            .notMatch("/auth/sendVerificationCode")
                            .notMatch("/auth/sendVerificationCodeByEmail")
//                            .notMatch("/auth/loginout")
                            .check(r -> StpUtil.checkLogin());
                    log.info("[Sa-Token] 鉴权成功");
                    // 权限认证 -- 不同模块, 校验不同权限
                    SaRouter.match("/auth/loginout", r -> StpUtil.checkPermission("app:note:publish"));
                    SaRouter.match("/auth/loginout", r -> StpUtil.checkRole("common_user"));
//                    SaRouter.match("/auth/user/loginout", r -> StpUtil.checkRole("admin"));
//                    SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));
//                    SaRouter.match("/goods/**", r -> StpUtil.checkPermission("goods"));
//                    SaRouter.match("/orders/**", r -> StpUtil.checkPermission("orders"));

                    // 更多匹配 ...  */
                })
                // 异常处理方法：每次setAuth函数出现异常时进入
//                .setError(e -> {
//                    return SaResult.error(e.getMessage());
//                })
                ;
    }
}

