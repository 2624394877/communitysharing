package com.taoxin.communitysharing.KV.enums;


import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ResponseStatusEnum implements BaseExceptionInterface {

    PARAMS_ERROR("kv-400", "参数错误"),
    PARAMS_NOT_VALID("kv-400", "参数校验失败"),
    SYSTEMP_ERROR("kv-502", "系统异常" ),
    UPLOAD_FILE_AVATAR_ERROR("kv-400", "上传头像失败"),
    UPLOAD_FILE_BACKGROUND_ERROR("kv-400", "上传背景图失败"),
    FILE_SIZE_ERROR("kv-500", "文件大小错误"),
    NOT_FOUND_CONTENT("kv-404", "未找到内容"),
    ;

    private final String ErrorCode;
    private final String ErrorMessage;
}
