package com.taoxin.communitysharing.common.validator.mail;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME) // 运行时保留
@Constraint(validatedBy = MailParamsValidator.class) // 验证器
public @interface MailParams {
    String message() default "邮箱格式错误"; // 错误信息
    Class<?>[] groups() default {}; // 分组
    Class<? extends Payload>[] payload() default {}; // 负载
}
