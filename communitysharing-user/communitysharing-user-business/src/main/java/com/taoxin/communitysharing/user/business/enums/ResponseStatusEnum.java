package com.taoxin.communitysharing.user.business.enums;


import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ResponseStatusEnum implements BaseExceptionInterface {

    PARAMS_ERROR("user-400", "参数错误"),
    PARAMS_NOT_VALID("user-400", "参数校验失败"),
    SYSTEMP_ERROR("user-502", "系统异常" ),
    NICK_NAME_STR_ERROR("user-500", "昵称格式错误"),
    COMMUNITYSHARING_ID_ERROR("user-500", "communitysharingId格式错误"),
    STRING_LENGTH_ERROR("user-500", "字符串长度错误"),
    GENDER_ERROR("user-500", "性别格式错误"),
    INVALID_GENDER("user-400", "无效的性别值"),
    SEX_NOT_SPECIFIED("user-400", "未指定性别"),
    EMAIL_ERROR("user-500", "邮箱格式错误"),
    PHONE_ERROR("user-500", "手机格式错误"),
    PASSWORD_ERROR("user-500", "密码格式错误"),
    UPLOAD_FILE_AVATAR_ERROR("user-400", "上传头像失败"),
    UPLOAD_FILE_BACKGROUND_ERROR("user-400", "上传背景图失败"),
    FILE_SIZE_ERROR("user-500", "文件大小错误"),
    USER_NOT_EXIST("user-400", "用户不存在"),
    EMAIL_NOT_EXIST("user-400", "邮箱未绑定"),
    UPDATE_USER_PASSWORD_ERROR("user-400", "更新用户密码失败"),
    EMAIL_HAVE_EXIST("user-400", "邮箱已有绑定账号"),
    EMAIL_FORMAT_ERROR("user-400", "邮箱格式错误"),
    VERIFICATION_CODE_ERROR("auth-4001", "验证码错误"),
    UPDATE_USER_MAIL_ERROR("user-400", "更新邮箱失败" ),
    PHONE_FORMAT_ERROR("user-500", "手机号格式错误"),
    PHONE_HAVE_EXIST("user-400", "手机号已注册账号"),
    UPDATE_USER_PHONE_ERROR("user-500", "更新手机号失败"),
    UPDATE_USER_FAIL("user-500", "更新用户信息失败"),
    ;

    private final String ErrorCode;
    private final String ErrorMessage;
}
