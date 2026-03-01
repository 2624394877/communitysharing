package com.taoxin.communitysharing.search.business.enums;


import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ResponseStatusEnum implements BaseExceptionInterface {

    PARAMS_ERROR("search-500", "参数错误"),
    PARAMS_NOT_VALID("search-401", "参数校验失败"),
    SYSTEMP_ERROR("search-502", "系统异常" ),
    CONTENT_GET_FAIL("search-503", "获取内容失败"),
    USER_GET_FAIL("search-504", "获取用户失败")
    ;

    private final String ErrorCode;
    private final String ErrorMessage;
}
