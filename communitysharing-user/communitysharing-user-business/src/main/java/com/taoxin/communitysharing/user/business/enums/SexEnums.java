package com.taoxin.communitysharing.user.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum SexEnums {
    MALE(1),
    FEMALE(0);

    private final Integer ganderValue;

    public static boolean isValidGender(Integer gender) {
        for (SexEnums value : SexEnums.values()) {
            if (Objects.equals(gender, value.ganderValue)) {
                return true;
            }
        }
        return false;
    }
}
