package com.taoxin.communitysharing.common.validator.phone;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // TODO: 验证手机号
        return value != null && value.matches("1\\d{10}");
    }

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        // TODO: 初始化
    } // 这个方法在校验器实例化后会被调用，通常用来读取注解中的参数来设置校验器的初始状态。
}
