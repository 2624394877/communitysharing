package com.taoxin.communitysharing.comment.business.enums;


import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ResponseStatusEnum implements BaseExceptionInterface {

    PARAMS_ERROR("comment-400", "参数错误"),
    PARAMS_NOT_VALID("comment-400", "参数校验失败"),
    SYSTEMP_ERROR("comment-502", "系统异常" ),
    FILE_SIZE_ERROR("comment-500", "文件大小错误"),
    COMMENT_NOT_EXIST("comment-500", "评论不存在"),
    ;

    private final String ErrorCode;
    private final String ErrorMessage;
}
