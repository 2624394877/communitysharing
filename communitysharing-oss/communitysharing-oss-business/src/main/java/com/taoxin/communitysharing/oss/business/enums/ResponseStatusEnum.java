package com.taoxin.communitysharing.oss.business.enums;


import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ResponseStatusEnum implements BaseExceptionInterface {

    FILE_UPLOAD_ERROR("oss-500", "文件上传失败"),
    FILE_DOWNLOAD_ERROR("oss-500", "文件下载失败"),
    PARAMS_ERROR("oss-400", "参数错误"),
    PARAMS_NOT_VALID("oss-400", "参数校验失败"),
    SYSTEMP_ERROR("oss-502", "系统异常" ),
    UPLOAD_FILE_EMPTY("oss-400", "上传文件为空"),
    UPLOAD_FILE_TOO_LARGE("oss-400", "上传文件过大"),
    UPLOAD_FILE_TYPE_NOT_SUPPORT("oss-400", "上传文件类型不支持"),
    UPLOAD_FILE_NAME_TOO_LONG("oss-400", "上传文件名过长"),
    UPLOAD_FILE_NAME_CONTAINS_ILLEGAL_CHARACTER("oss-400", "上传文件名包含非法字符"),
    UPLOAD_FILE_FAIL("oss-400", "上传文件失败"),
    ;

    private final String ErrorCode;
    private final String ErrorMessage;
}
