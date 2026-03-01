package com.taoxin.communitysharing.framework.business.operationlog.aspect;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 自定义AOP切面类
 */
@Aspect
@Slf4j
public class ApiOperationLogAspect {
    //1. 添加切点 使用注解作为切点，表示再任何方法上添加ApiOperationLog注解都表示需要记录日志
    @Pointcut("@annotation(com.taoxin.communitysharing.framework.business.operationlog.aspect.ApiOperationLog)")
    public void apiOperationLog() {} // 定义切点 该方法什么都不做，只是为@Pointcut注解提供标识

    //2. 添加通知

    /**
     * 环绕通知，该方法拦截所有使用ApiOperationLog注解的方法；
     * @方法的编写逻辑：
     * 1. 获取方法参数
     * 2. 获取功能描述信息
     * 3. 获取方法返回值
     * 4. 获取方法执行时间
     * 5. 打印和记录日志
     * @joinPoint 切点对象，封装了方法参数等信息
     */
    @Around("apiOperationLog()") // 在切点apiOperationLog()上添加通知
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取方法参数
        String className = joinPoint.getTarget().getClass().getName(); // 获取类名
        String methodName = joinPoint.getSignature().getName(); // 获取方法名
        Object[] args = joinPoint.getArgs(); // 获取方法参数
        String params = Arrays.stream(args).map(toJsonStr()).collect(Collectors.joining(",")); // 将参数转换为JSON字符串(自定义)
        // 2. 获取功能描述信息
        String description = getApiOperationLogDescription(joinPoint); // 获取功能描述信息（自定义）
        // 5. 打印日志
        log.info("====== 请求开始: [{}], 入参: {}, 请求类: {}, 请求方法: {} =================================== ",
                description,params,className,methodName);
        // 4. 获取方法执行时间
        long startTime = System.currentTimeMillis();
        // 3. 获取方法返回值
        Object result = joinPoint.proceed(); // 执行切点方法
        // 4. 获取方法执行时间
        long executionTime = System.currentTimeMillis() - startTime;
        // 5. 打印日志
        log.info("====== 响应结束: [{}], 耗时: {}ms, 返回参数: {} =================================== ",
                description,executionTime, JsonUtil.toJsonString(result));
        return result; // 返回方法执行结果
    }

    /**
     * 获取方法描述
     * @param joinPoint
     * @return
     */
    private String getApiOperationLogDescription(ProceedingJoinPoint joinPoint){
        // 1. 获取 MethodSignature 对象，用于获取方法描述
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        //2. 获取注解的当前方法
        Method method = methodSignature.getMethod();

        // 3. 当前方法对象中获取自定义切点注解的实例
        ApiOperationLog apiOperationLog = method.getAnnotation(ApiOperationLog.class);

        // 4. 获取实例中的描述属性值并返回
        return apiOperationLog.description();
    }

    /**
     * 将对象转换为JSON字符串的函数
     * @return Function<Object,String> 转换函数
     */
    private Function<Object,String> toJsonStr() {
        return JsonUtil::toJsonString;
    }
}
