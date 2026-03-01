package com.taoxin.communitysharing.content.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ContentTypeEnum {
    IMAGE(0, "图文"),
    VIDEO(1, "音视频"),
    LINK(2, "链接"),
    FILE(3, "文件"),
    ;

    private final Integer code;
    private final String message;

    /**
     * 验证类型是否合法
     * @param code  类型
     * @return 是否合法
     */
    public static boolean isValid(Integer code) {
        for (ContentTypeEnum contentTypeEnum : ContentTypeEnum.values()) {
            if (Objects.equals(contentTypeEnum.code, code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取枚举
     * @param code  类型
     * @return 枚举
     */
    public static ContentTypeEnum getEnum(Integer code) {
        for (ContentTypeEnum contentTypeEnum : ContentTypeEnum.values()) {
            if (Objects.equals(contentTypeEnum.code, code)) {
                return contentTypeEnum;
            }
        }
        return null;
    }
}
