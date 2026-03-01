package com.taoxin.communitysharing.oss.business.exception;

import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.common.response.Response;

import com.taoxin.communitysharing.oss.business.enums.ResponseStatusEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@ControllerAdvice // 标识全局异常处理类
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param request HTTP请求
     * @param e 业务异常
     * @return 响应对象
     */
    @ExceptionHandler(BusinessException.class) // 拦截业务异常
    @ResponseBody // 返回JSON数据
    public Response<Object> handleBizException(HttpServletRequest request, BusinessException e) {
        log.warn("响应: {}请求失败, 错误码: {}, 错误信息: {}", request.getRequestURI(), e.getErrorCode(), e.getErrorMessage());
        return Response.fail(e);
    }

    /**
     * 处理参数校验异常
     * @param request HTTP请求
     * @param e 参数校验异常
     * @return 响应对象
     */
    @ExceptionHandler({ MethodArgumentNotValidException.class }) // 拦截参数校验异常
    @ResponseBody
    public Response<Object> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        // 获取错误异常码
        String errorCode = ResponseStatusEnum.PARAMS_NOT_VALID.getErrorCode();

        // 获取错误信息
        BindingResult bindingResult = e.getBindingResult();

        // 创建StringBuilder对象
        StringBuilder stringbuilder = new StringBuilder();

        // 获取校验不通过的字段，并组合错误信息，格式为： email 邮箱格式不正确, 当前值: '123124qq.com'
        Optional.ofNullable(bindingResult.getFieldErrors()).ifPresent(fieldErrors -> {
            fieldErrors.forEach(fieldError -> {
                stringbuilder
//                        .append(fieldError.getField())
                        .append(fieldError.getDefaultMessage())
                        .append(",当前值: ")
                        .append(fieldError.getRejectedValue());
            });
        });
        // 将拼接的错误信息的StringBuilder对象转为字符串
        String errorMessage = stringbuilder.toString();
        log.warn("响应: {}请求失败, 错误码: {}, 错误信息: {}", request.getRequestURI(), errorCode, errorMessage);
        return Response.fail(errorCode, errorMessage);
    }

    /**
     * 处理参数校验异常（Guava 库中的 Preconditions）
     * @param request HTTP请求
     * @param illegalArgumentException 参数校验异常
     * @return 响应对象
     */
//    @ExceptionHandler({ IllegalArgumentException.class })
//    @ResponseBody
//    public Response<Object> handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException illegalArgumentException) {
//        log.warn("响应: {}请求失败, 错误码: {}, 错误信息: {}", request.getRequestURI(), ResponseStatusEnum.VERIFICATION_CODE_ERROR.getErrorCode(), illegalArgumentException.getMessage());
//        return Response.fail(ResponseStatusEnum.VERIFICATION_CODE_ERROR.getErrorCode(), illegalArgumentException.getMessage());
//    }


    /**
     * 处理系统异常
     * @param request HTTP请求
     * @param e 系统异常
     * @return 响应对象
     */
    @ExceptionHandler({ Exception.class })
    @ResponseBody
    public Response<Object> handleOtherException(HttpServletRequest request, Exception e) {
        log.error("响应: {} 请求错误,", request.getRequestURI(), e);
        return Response.fail(ResponseStatusEnum.SYSTEMP_ERROR.getErrorCode(), e.getMessage());
    }
}
