package com.taoxin.communitysharing.content.business.enums;


import com.taoxin.communitysharing.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ResponseStatusEnum implements BaseExceptionInterface {

    PARAMS_ERROR("content-400", "参数错误"),
    PARAMS_NOT_VALID("content-400", "参数校验失败"),
    SYSTEMP_ERROR("content-502", "系统异常" ),
    CONTENT_TYPE_UNDIFF("content-501", "内容类型错误"),
    CONTENT_PUBLISH_ERROR("content-400", "内容发布失败"),
    CONTENT_NOT_EXIST("content-404", "内容不存在"),
    CONTENT_NOT_VISIBLE("content-203", "内容不可见"),
    PATH_ERROR("content-504", "请求链接错误"),
    CONTENT_NOT_UPDATED("content-400", "内容更新失败"),
    TOPIC_NOT_EXIST("content-404", "话题不存在"),
    CONTENT_CANT_PRIVATE("content-400", "内容不能私密"),
    CONTENT_PUBLIC_FAIL("content-400", "公开内容失败"),
    CONTENT_TOP_FAIL("content-400", "内容置顶失败"),
    CONTENT_CANCEL_TOP_FAIL("content-400", "内容取消置顶失败"),
    CANT_UPDATE_YOURSELF_CONTENT("content-502", "不能更新非自己的内容"),
    CONTENT_LIKE_FAIL("content-401", "内容点赞失败"),
    CONTENT_ALREADY_LIKED("content-501", "内容已点赞! 无法再次点赞"),
    CONTENT_LIKE_ERROR("content-400", "内容点赞异常"),
    CONTENT_NOT_LIKED("content-400", "内容未点赞! 不能取消点赞"),
    CONTENT_COLLECTED("content-501", "内容已收藏! 无法再次收藏"),
    CONTENT_NOT_COLLECTED("content-400", "内容未收藏! 不能取消收藏"),
    CONTENT_COLLECT_FAIL("content-400", "内容收藏失败"),;

    private final String ErrorCode;
    private final String ErrorMessage;
}
