package com.taoxin.communitysharing.framework.business.operationlog.aspect;

import java.lang.annotation.*;


/**
 * API操作日志注解
 * @description 描述API操作日志
 * @注解说明：
 * @Retention 保留注解 RetentionPolicy.RUNTIME参数表示：注解保留在运行时
 * @Target 注解作用目标 ElementType.METHOD参数表示：方法
 * @Documented 注解是否被抽取到文档中
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface ApiOperationLog {
    String description() default "";
}
