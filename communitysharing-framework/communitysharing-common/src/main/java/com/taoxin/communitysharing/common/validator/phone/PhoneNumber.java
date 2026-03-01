package com.taoxin.communitysharing.common.validator.phone;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE}) // 指定注解作用位置
@Retention(RetentionPolicy.RUNTIME) // 指定注解保留策略
@Constraint(validatedBy = PhoneNumberValidator.class) // 指定注解的验证器
public @interface PhoneNumber {
    String message() default "手机号格式错误"; // 错误信息
    Class<?>[] groups() default {}; // 分组
    Class<? extends Payload>[] payload() default {}; // 负载
}
