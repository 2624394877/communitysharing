package com.taoxin.communitysharing.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常类
 */
@Getter
@Setter
public class BusinessException extends RuntimeException{
    private String errorCode;
    private String errorMessage;

    /**
     * 作用： 将自定义异常接口的实现类传入，并赋值给errorCode和errorMessage
     * @param bei 自定义异常接口的实现类
     */
    public BusinessException(BaseExceptionInterface bei) {
        this.errorCode = bei.getErrorCode();
        this.errorMessage = bei.getErrorMessage();
    }

    public BusinessException(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
