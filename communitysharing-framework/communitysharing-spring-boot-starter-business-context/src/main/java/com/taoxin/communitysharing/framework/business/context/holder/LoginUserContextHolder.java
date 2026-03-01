package com.taoxin.communitysharing.framework.business.context.holder;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.taoxin.communitysharing.common.constant.GlobalConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ThreadLocal+过滤器实现用户上下文传递
 */
public class LoginUserContextHolder {
    // 定义一个线程本地变量
    private static final ThreadLocal<Map<String, Object>> LOGIN_USER_CONTEXT_THREAD_LOCAL = TransmittableThreadLocal.withInitial(HashMap::new);


    /**
     * 设置用户ID
     * @param value
     */
    public static void setUserId(Object value) {
        LOGIN_USER_CONTEXT_THREAD_LOCAL.get().put(GlobalConstants.USER_ID,value); // 设置用户ID
    }

    /**
     * 获取用户
     * @return 用户ID
     */
    public static Long getUserId() {
        Object value = LOGIN_USER_CONTEXT_THREAD_LOCAL.get().get(GlobalConstants.USER_ID);
        if (Objects.isNull(value)) {
            return null;
        }
        return Long.valueOf(value.toString());
    }

    /**
     * 移除用户
     */
    public static void remove() {
        LOGIN_USER_CONTEXT_THREAD_LOCAL.remove();
    }
}
