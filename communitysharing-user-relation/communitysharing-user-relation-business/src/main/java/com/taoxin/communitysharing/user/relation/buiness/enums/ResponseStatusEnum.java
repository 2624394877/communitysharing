package com.taoxin.communitysharing.user.relation.buiness.enums;


import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ResponseStatusEnum implements BaseExceptionInterface {

    PARAMS_ERROR("user-relation-400", "参数错误"),
    PARAMS_NOT_VALID("user-relation-400", "参数校验失败"),
    SYSTEMP_ERROR("user-relation-502", "系统异常" ),
    FOLLOWING_USER_ERROR("user-relation-500", "关注用户失败"),
    CANT_FOLLOWING_YOURSELF("user-relation-500", "不能关注自己"),
    USER_NOT_EXIST("user-relation-500", "用户不存在"),
    FOLLOW_NUMBER_OVER_MAX_SIZE("user-relation-500", "关注数已经达到上限"),
    FOLLOWED_USER("user-relation-500", "用户已关注"),
    UN_FOLLOWING_USER_ERROR("user-relation-500", "取消关注用户失败"),
    UN_FOLLOWED_USER("user-relation-500", "用户未关注"),
    CANT_UN_FOLLOWING_YOURSELF("user-relation-500", "不能取消关注自己"),
    PAGE_NUMBER_ERROR("user-relation-500", "页码错误");
    private final String ErrorCode;
    private final String ErrorMessage;
}
