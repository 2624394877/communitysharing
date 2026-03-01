package com.taoxin.communitysharing.auth.enums;


import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ResponseStatusEnum implements BaseExceptionInterface {

    SYSTEMP_ERROR("auth-500", "系统异常"),
    PARAMS_NOT_VALID("auth-400","参数无效"),
    METHOD_NOT_ALLOWED("auth-405", "方法不允许"),
    SERVICE_UNAVAILABLE("auth-503", "服务不可用"),
    DUPLICATE_ENTRY("auth-4006", "重复条目"),
    DATA_INTEGRITY_VIOLATION("auth-4005", "数据完整性违规"),
    USER_NOT_FOUND("auth-4001", "用户不存在"),
    REQUEST_TIMEOUT("auth-408", "请求超时"),
    TOO_MANY_REQUESTS("auth-429", "请求过于频繁，请稍后再试"),
    REQUEST_FREQUENT("auth-4291", "接口调用过于频繁"),
    RATE_LIMIT_EXCEEDED("auth-4292", "访问频率超限"),

    EMAIL_NOT_FOUND("auth-4003", "邮箱不存在,请使用绑定的邮箱"),
    EMAIL_SEND_ERROR("auth-4002", "邮件发送失败"),

    // 验证码错误
    VERIFICATION_CODE_ERROR("auth-4001", "验证码错误或已过期"),
    UPDATE_USER_ERROR("auth-4007", "更新失败"),
    PASSWORD_ERROR("auth-4004", "密码错误"),
    PASSWORD_FORMAT_ERROR("auth-4005", "密码格式错误"),

    LOGIN_FAILED("auth-4000", "登录失败"),
    ;


    private final String ErrorCode;
    private final String ErrorMessage;
}
