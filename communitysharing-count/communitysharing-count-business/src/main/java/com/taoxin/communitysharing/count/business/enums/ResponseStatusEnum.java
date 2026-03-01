package com.taoxin.communitysharing.count.business.enums;


import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ResponseStatusEnum implements BaseExceptionInterface {

    PARAMS_ERROR("count-400", "参数错误"),
    PARAMS_NOT_VALID("count-400", "参数校验失败"),
    SYSTEMP_ERROR("count-502", "系统异常" ),
    ;

    private final String ErrorCode;
    private final String ErrorMessage;
}
