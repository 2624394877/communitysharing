package com.taoxin.communitysharing.gateway.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.taoxin.communitysharing.common.response.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taoxin.communitysharing.gateway.enums.ResponseStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static cn.hutool.core.exceptions.ExceptionUtil.isCausedBy;

@Component
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    @Resource
    ObjectMapper objectMapper;
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 获取响应对象
        ServerHttpResponse response = exchange.getResponse();
        // 设置响应参数
        Response<?> result;
        // 根据捕获的异常类型，设置不同的响应状态码和响应消息
        if (isCausedBy(ex, NotPermissionException.class)) { // SaToken异常
            response.setStatusCode(HttpStatus.FORBIDDEN); // 403
            result = Response.fail(ResponseStatusEnum.ROLE_PERMISSION_DENIE.getErrorCode(),ResponseStatusEnum.ROLE_PERMISSION_DENIE.getErrorMessage());
        } else if (isCausedBy(ex, NotRoleException.class)) {
            response.setStatusCode(HttpStatus.FORBIDDEN); // 403
            result = Response.fail(ResponseStatusEnum.NOT_ROLE.getErrorCode(),ResponseStatusEnum.NOT_ROLE.getErrorMessage());
        } else if (isCausedBy(ex, NotLoginException.class)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // 401
            if(NotLoginException.TOKEN_TIMEOUT_MESSAGE.equals(ex.getMessage())) {
                result = Response.fail(ResponseStatusEnum.TOKEN_EXPIRED.getErrorCode(),ResponseStatusEnum.TOKEN_EXPIRED.getErrorMessage());
            } else {
                result = Response.fail(ResponseStatusEnum.NOT_LOGIN.getErrorCode(), ResponseStatusEnum.NOT_LOGIN.getErrorMessage());
            }
        } else {
            log.error(ex.getMessage(), ex);
            result = Response.fail(ResponseStatusEnum.SYSTEM_ERROR);
        }

        // 设置响应头为JSON格式
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
        return response.writeWith(Mono.fromSupplier(() -> { // 使用 Mono.fromSupplier 创建响应体
            // 创建DataBuffer
            DataBufferFactory bufferFactory = response.bufferFactory();
            try{
                // 将响应数据转换为字节数组
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(result));
            } catch (Exception e) {
                // 如果转换过程中出现异常，则返回空字节数组
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}
