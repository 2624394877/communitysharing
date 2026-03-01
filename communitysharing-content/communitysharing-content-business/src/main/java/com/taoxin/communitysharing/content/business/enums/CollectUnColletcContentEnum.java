package com.taoxin.communitysharing.content.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CollectUnColletcContentEnum {
    COLLECT(1),
    UNCOLLECTED(0);
    private final Integer status;
}
