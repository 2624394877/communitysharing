package com.taoxin.communitysharing.common.response;

import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import com.taoxin.communitysharing.common.exception.BusinessException;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应类，封装了请求响应的数据格式，包括响应状态、错误码、错误信息和响应数据
 * 支持泛型，可以灵活处理各种类型的响应数据
 * 提供了静态方法用于创建成功和失败的响应实例
 * Serializable:
 */
@Data //  添加getter和setter方法
public class Response<T> implements Serializable {

    private boolean success = true; // 响应状态标志
    private String errorCode; // 响应错误码
    private String message; // 响应信息
    private T data; // 响应数据 这里用泛型

    /**
     * 无参success函数，将返回一个默认的成功响应
     * @return 成功状态的Response实例
     * @param <T> 泛型参数，表示响应数据的类型
     */
    public static <T> Response<T> success() {
        return new Response<>();
    }

    /**
     * 带参success函数，将返回一个包含指定数据的成功响应
     * @param data 响应数据
     * @return 成功状态的Response实例，包含指定数据
     * @param <T> 泛型参数，表示响应数据的类型
     */
    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setData(data);
        return response;
    }


    /**
     * 无参fail函数，将返回一个默认的失败响应
     * @return 失败状态的Response实例
     * @param <T> 泛型参数，表示响应数据的类型
     */
    public static <T> Response<T> fail() {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        return response;
    }

    /**
     * 带错误信息的fail函数，将返回一个包含指定错误信息的失败响应
     * @param errorMessage 错误信息
     * @return 失败状态的Response实例，包含指定错误信息
     * @param <T> 泛型参数，表示响应数据的类型
     */
    public static <T> Response<T> fail(String errorMessage) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setMessage(errorMessage);
        return response;
    }


    /**
     * 带错误码和错误信息的fail函数，将返回一个包含指定错误码和错误信息的失败响应
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     * @return 失败状态的Response实例，包含指定错误码和错误信息
     * @param <T> 泛型参数，表示响应数据的类型
     */
    public static <T> Response<T> fail(String errorCode,String errorMessage) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(errorMessage);
        return response;
    }


    /**
     * 根据BaseExceptionInterface创建失败响应
     * @param bei 包含错误码和错误信息的异常接口实现
     * @return 失败状态的Response实例，包含指定错误码和错误信息
     * @param <T> 泛型参数，表示响应数据的类型
     */
    public static <T> Response<T> fail(BaseExceptionInterface bei) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setErrorCode(bei.getErrorCode());
        response.setMessage(bei.getErrorMessage());
        return response;
    }


    /**
     * 根据BusinessException创建失败响应
     * @param be 业务异常对象
     * @return 失败状态的Response实例，包含指定错误码和错误信息
     * @param <T> 泛型参数，表示响应数据的类型
     */
    public static <T> Response<T> fail(BusinessException be) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setErrorCode(be.getErrorCode());
        response.setMessage(be.getErrorMessage());
        return response;
    }
}
