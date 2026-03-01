package com.taoxin.communitysharing.content.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // 创建Getter方法
@AllArgsConstructor // 创建有参构造函数
public enum ContentLikeLuaResultEnum {
    LIKED_SUCCESS(0L), // 点赞成功
    // 布隆过滤器中没有这个内容id
    NOT_EXIST(-1L),
    // 布隆过滤器中有内容id 表示已点赞
    BLOOM_LIKED(1L);
    private final Long code;

    /**
     * 根据code获取枚举实例
     * @param code 枚举的code值
     * @return 对应code的枚举实例，如果没有找到则返回null
     */
    public static ContentLikeLuaResultEnum valueOf(Long code) {
        for (ContentLikeLuaResultEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
